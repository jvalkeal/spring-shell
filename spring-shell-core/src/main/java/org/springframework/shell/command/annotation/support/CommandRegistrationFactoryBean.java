/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.shell.command.annotation.support;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;
import org.springframework.shell.Utils;
import org.springframework.shell.command.CommandExceptionResolver;
import org.springframework.shell.command.CommandHandlingResult;
import org.springframework.shell.command.CommandRegistration;
import org.springframework.shell.command.CommandRegistration.Builder;
import org.springframework.shell.command.CommandRegistration.OptionArity;
import org.springframework.shell.command.CommandRegistration.OptionSpec;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.shell.command.annotation.OptionValues;
import org.springframework.shell.command.annotation.ExceptionResolverMethodResolver;
import org.springframework.shell.command.invocation.InvocableShellMethod;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * Factory bean used in {@link CommandRegistrationBeanRegistrar} to build
 * instance of {@link CommandRegistration}. Main logic of constructing
 * {@link CommandRegistration} out from annotated command target is
 * in this factory.
 *
 * This factory needs a name of a {@code commandBeanName} which is a bean
 * hosting command methods, {@code method} which is a {@link Method} in
 * a bean and optionally {@code BuilderSupplier} if context provides
 * pre-configured builder.
 *
 * This is internal class and not meant for generic use.
 *
 * @author Janne Valkealahti
 */
class CommandRegistrationFactoryBean implements FactoryBean<CommandRegistration>, ApplicationContextAware, InitializingBean {

	private final Logger log = LoggerFactory.getLogger(CommandRegistrationFactoryBean.class);
	public static final String COMMAND_BEAN_TYPE = "commandBeanType";
	public static final String COMMAND_BEAN_NAME = "commandBeanName";
	public static final String COMMAND_METHOD_NAME = "commandMethodName";
	public static final String COMMAND_METHOD_PARAMETERS = "commandMethodParameters";

	private ObjectProvider<CommandRegistration.BuilderSupplier> supplier;
	private ApplicationContext applicationContext;
	private Object commandBean;
	private Class<?>[] commandBeanType;
	private String commandBeanName;
	private String commandMethodName;
	private Class<?>[] commandMethodParameters;

	@Override
	public CommandRegistration getObject() throws Exception {
		CommandRegistration registration = buildRegistration();
		return registration;
	}

	@Override
	public Class<?> getObjectType() {
		return CommandRegistration.class;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.commandBean = applicationContext.getBean(commandBeanName);
		this.supplier = applicationContext.getBeanProvider(CommandRegistration.BuilderSupplier.class);
	}

	public void setCommandBeanType(Class<?>[] commandBeanType) {
		this.commandBeanType = commandBeanType;
	}

	public void setCommandBeanName(String commandBeanName) {
		this.commandBeanName = commandBeanName;
	}

	public void setCommandMethodName(String commandMethodName) {
		this.commandMethodName = commandMethodName;
	}

	public void setCommandMethodParameters(Class<?>[] commandMethodParameters) {
		this.commandMethodParameters = commandMethodParameters;
	}

	private CommandRegistration.Builder getBuilder() {
		return supplier.getIfAvailable(() -> () -> CommandRegistration.builder()).get();
	}

	private CommandRegistration buildRegistration() {
		Method method = ReflectionUtils.findMethod(commandBean.getClass(), commandMethodName, commandMethodParameters);

		MergedAnnotation<Command> classAnnotation = MergedAnnotations.from(commandBean.getClass(), SearchStrategy.TYPE_HIERARCHY)
				.get(Command.class);

		MergedAnnotation<Command> methodAnnotation = MergedAnnotations.from(method, SearchStrategy.TYPE_HIERARCHY)
				.get(Command.class);

		boolean deduceHidden = CommandAnnotationUtils.deduceHidden(classAnnotation, methodAnnotation);


		Builder builder = getBuilder();

		String[] deduceCommand = CommandAnnotationUtils.deduceCommand(classAnnotation, methodAnnotation);
		if (deduceCommand.length == 0) {
			deduceCommand = new String[] { Utils.unCamelify(method.getName()) };
		}
		builder.command(deduceCommand);

		Command ann = AnnotatedElementUtils.findMergedAnnotation(method, Command.class);
		// String[] keys = ann.command();
		// if (keys.length == 0) {
		// 	keys = new String[] { Utils.unCamelify(method.getName()) };
		// }
		// String key = keys[0];
		// builder.command(key);

		// builder.hidden(ann.hidden());
		builder.hidden(deduceHidden);

		String deduceGroup = CommandAnnotationUtils.deduceGroup(classAnnotation, methodAnnotation);
		builder.group(deduceGroup);

		builder.description(ann.description());
		builder.interactionMode(ann.interactionMode());

		// Supplier<Availability> availabilityIndicator = findAvailabilityIndicator(keys, bean, method);

		// builder.availability(availabilityIndicator)

		String[] deduceAlias = CommandAnnotationUtils.deduceAlias(classAnnotation, methodAnnotation);
		if (deduceAlias.length > 0) {
			builder.withAlias().command(deduceAlias);
		}

		// builder.withAlias().command(deduceCommand)

		builder.withTarget().method(commandBean, method);

		InvocableHandlerMethod ihm = new InvocableHandlerMethod(commandBean, method);
		for (MethodParameter mp : ihm.getMethodParameters()) {
			onCommandParameter(mp, builder);
		}

		ExceptionResolverMethodResolver exceptionResolverMethodResolver = new ExceptionResolverMethodResolver(commandBean.getClass());
		MethodCommandExceptionResolver methodCommandExceptionResolver = new MethodCommandExceptionResolver();
		methodCommandExceptionResolver.bean = commandBean;
		methodCommandExceptionResolver.exceptionResolverMethodResolver = exceptionResolverMethodResolver;
		builder.withErrorHandling().resolver(methodCommandExceptionResolver);

		CommandRegistration registration = builder.build();
		return registration;
	}

	private void onCommandParameter(MethodParameter mp, Builder builder) {
		Option so = mp.getParameterAnnotation(Option.class);
		log.debug("Registering with mp='{}' so='{}'", mp, so);
		if (so != null) {
			List<String> longNames = new ArrayList<>();
			List<Character> shortNames = new ArrayList<>();
			for(int i = 0; i < so.shortNames().length; i++) {
				shortNames.add(so.shortNames()[i]);
			}
			if (!ObjectUtils.isEmpty(so.longNames())) {
				for(int i = 0; i < so.longNames().length; i++) {
					longNames.add(so.longNames()[i]);
				}
			}
			else {
				// ShellOption value not defined
				mp.initParameterNameDiscovery(new DefaultParameterNameDiscoverer());
				String longName = mp.getParameterName();
				Class<?> parameterType = mp.getParameterType();
				if (longName != null) {
					log.debug("Using mp='{}' longName='{}' parameterType='{}'", mp, longName, parameterType);
					longNames.add(longName);
				}
			}
			if (!longNames.isEmpty() || !shortNames.isEmpty()) {
				log.debug("Registering longNames='{}' shortNames='{}'", longNames, shortNames);
				Class<?> parameterType = mp.getParameterType();
				OptionSpec optionSpec = builder.withOption();
				optionSpec.type(parameterType);
				optionSpec.longNames(longNames.toArray(new String[0]));
				optionSpec.shortNames(shortNames.toArray(new Character[0]));
				optionSpec.position(mp.getParameterIndex());
				optionSpec.description(so.description());
				if (so.arityMin() >= 0 || so.arityMax() >= 0) {
					optionSpec.arity(Math.max(0, so.arityMin()), Math.max(so.arityMin(), so.arityMax()));
				}
				else {
					if (ClassUtils.isAssignable(boolean.class, parameterType)) {
						optionSpec.arity(OptionArity.ZERO);
					}
					else if (ClassUtils.isAssignable(Boolean.class, parameterType)) {
						optionSpec.arity(OptionArity.ZERO);
					}
					else {
						optionSpec.arity(OptionArity.EXACTLY_ONE);
					}
				}

				if (StringUtils.hasText(so.defaultValue())) {
					optionSpec.defaultValue(so.defaultValue());
				}
				else if (ClassUtils.isAssignable(boolean.class, parameterType)){
					optionSpec.required(false);
					optionSpec.defaultValue("false");
				}
				else {
					optionSpec.required();
				}


				OptionValues ovAnn = mp.getParameterAnnotation(OptionValues.class);
				if (ovAnn != null && StringUtils.hasText(ovAnn.ref())) {
					// CompletionProvider cr = this.applicationContext.getBean(ovAnn.ref(), CompletionProvider.class);
					// if (cr != null) {
					// 	optionSpec.completion(ctx -> cr.apply(ctx));
					// }
				}

			}
		}
		else {
			mp.initParameterNameDiscovery(new DefaultParameterNameDiscoverer());
			String longName = mp.getParameterName();
			Class<?> parameterType = mp.getParameterType();
			if (longName != null) {
				log.debug("Using mp='{}' longName='{}' parameterType='{}'", mp, longName, parameterType);
				OptionSpec optionSpec = builder.withOption();
				optionSpec.longNames(longName);
				optionSpec.type(parameterType);
				optionSpec.required();
				optionSpec.position(mp.getParameterIndex());
				if (ClassUtils.isAssignable(boolean.class, parameterType)) {
					optionSpec.arity(OptionArity.ZERO);
				}
				else if (ClassUtils.isAssignable(Boolean.class, parameterType)) {
					optionSpec.arity(OptionArity.ZERO);
				}
				else {
					optionSpec.arity(OptionArity.EXACTLY_ONE);
				}
			}
		}
	}

	private static class MethodCommandExceptionResolver implements CommandExceptionResolver {

		Object bean;
		ExceptionResolverMethodResolver exceptionResolverMethodResolver;

		@Override
		public CommandHandlingResult resolve(Exception ex) {
			Method exceptionHandlerMethod = exceptionResolverMethodResolver.resolveMethodByThrowable(ex);
			InvocableShellMethod invocable = new InvocableShellMethod(bean, exceptionHandlerMethod);
			try {
				Object invoke = invocable.invoke(null, ex);
				if (invoke instanceof CommandHandlingResult) {
					return (CommandHandlingResult)invoke;
				}
			} catch (Exception e) {
			}
			return null;
		}

	}
}
