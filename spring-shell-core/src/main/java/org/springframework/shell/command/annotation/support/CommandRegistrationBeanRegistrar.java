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
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodIntrospector;
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
import org.springframework.shell.command.annotation.ExceptionResolverMethodResolver;
import org.springframework.shell.command.annotation.Option;
import org.springframework.shell.command.annotation.OptionValues;
import org.springframework.shell.command.invocation.InvocableShellMethod;
import org.springframework.shell.completion.CompletionProvider;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.ReflectionUtils.MethodFilter;

/**
 * Delegate used by {@link EnableCommandRegistrar} to register a bean definition
 * for a {@link Command @Command} class.
 *
 * @author Janne Valkealahti
 */
public final class CommandRegistrationBeanRegistrar {

	private final Logger log = LoggerFactory.getLogger(CommandRegistrationBeanRegistrar.class);
	private final BeanDefinitionRegistry registry;
	private final BeanFactory beanFactory;
	private static final MethodFilter COMMAND_METHODS = method ->
			AnnotatedElementUtils.hasAnnotation(method, Command.class);

	public CommandRegistrationBeanRegistrar(BeanDefinitionRegistry registry) {
		this.registry = registry;
		this.beanFactory = (BeanFactory) this.registry;
	}

	public void register(Class<?> type) {
		MergedAnnotation<Command> annotation = MergedAnnotations.from(type, SearchStrategy.TYPE_HIERARCHY)
				.get(Command.class);
		register(type, annotation);
	}

	void register(Class<?> type, MergedAnnotation<Command> annotation) {
		// String name = getName(type, annotation);
		String name = type.getName();
		if (!containsBeanDefinition(name)) {
			registerBeanDefinition(name, type, annotation);
		}
		xxx(type, name);
	}

	void xxx(Class<?> type, String containerBean) {
		// List<CommandRegistration> registrations = new ArrayList<>();
		Set<Method> methods = MethodIntrospector.selectMethods(type, COMMAND_METHODS);

		methods.forEach(m -> {
			// BeanDefinition asdf = createCommandMethodBeanDefinition(containerBean);
			String beanName = UUID.randomUUID().toString();
			this.registry.registerBeanDefinition(beanName, createCommandMethodBeanDefinition(containerBean, m));
		});

		// methods.forEach(m -> onCommand(registrations, m, bean, type));

	}

	// private String getName(Class<?> type, MergedAnnotation<Command> annotation) {
	// 	String prefix = annotation.isPresent() ? annotation.getString("prefix") : "";
	// 	return (StringUtils.hasText(prefix) ? prefix + "-" + type.getName() : type.getName());
	// }

	private boolean containsBeanDefinition(String name) {
		return containsBeanDefinition(this.beanFactory, name);
	}

	private boolean containsBeanDefinition(BeanFactory beanFactory, String name) {
		if (beanFactory instanceof ListableBeanFactory listableBeanFactory
				&& listableBeanFactory.containsBeanDefinition(name)) {
			return true;
		}
		if (beanFactory instanceof HierarchicalBeanFactory hierarchicalBeanFactory) {
			return containsBeanDefinition(hierarchicalBeanFactory.getParentBeanFactory(), name);
		}
		return false;
	}

	private void registerBeanDefinition(String beanName, Class<?> type,
			MergedAnnotation<Command> annotation) {
		Assert.state(annotation.isPresent(), () -> "No " + Command.class.getSimpleName()
				+ " annotation found on  '" + type.getName() + "'.");
		this.registry.registerBeanDefinition(beanName, createCommandClassBeanDefinition(type));
	}

	private BeanDefinition createBeanDefinition(String beanName, Class<?> type) {
		// BindMethod bindMethod = BindMethod.get(type);
		RootBeanDefinition definition = new RootBeanDefinition(type);
		// definition.setAttribute(BindMethod.class.getName(), bindMethod);
		// if (bindMethod == BindMethod.VALUE_OBJECT) {
		// 	definition.setInstanceSupplier(() -> ConstructorBound.from(this.beanFactory, beanName, type));
		// }
		return definition;
	}

	private BeanDefinition createCommandClassBeanDefinition(Class<?> type) {
		RootBeanDefinition definition = new RootBeanDefinition(type);
		return definition;
	}

	private BeanDefinition createCommandMethodBeanDefinition(String containerBean, Method method) {
		RootBeanDefinition definition = new RootBeanDefinition(CommandRegistrationFactoryBean.class);
		// definition.setAttribute("commandBeanName", containerBean);
		MutablePropertyValues mpv = new MutablePropertyValues();
		mpv.add("commandBeanName", containerBean);
		mpv.add("commandMethod", method);
		definition.setPropertyValues(mpv);
		// definition.setInstanceSupplier(null);
		return definition;
	}

	private void onCommand(List<CommandRegistration> registrations, Method method, Object bean, Class<?> clazz) {
		Command ann = AnnotatedElementUtils.findMergedAnnotation(method, Command.class);

		// XXX how to get configured builder?
		// Builder builder = commandRegistrationBuilderSupplier.get();
		Builder builder = CommandRegistration.builder();

		String[] keys = ann.command();
		if (keys.length == 0) {
			keys = new String[] { Utils.unCamelify(method.getName()) };
		}
		String key = keys[0];

		builder.command(key);
		builder.hidden(ann.hidden());
		builder.group(ann.group());
		builder.description(ann.description());
		builder.interactionMode(ann.interactionMode());

		// Supplier<Availability> availabilityIndicator = findAvailabilityIndicator(keys, bean, method);

		// builder.availability(availabilityIndicator)

		builder.withTarget().method(bean, method);

		InvocableHandlerMethod ihm = new InvocableHandlerMethod(bean, method);
		for (MethodParameter mp : ihm.getMethodParameters()) {
			onCommandParameter(mp, builder);
		}

		ExceptionResolverMethodResolver exceptionResolverMethodResolver = new ExceptionResolverMethodResolver(clazz);
		MethodCommandExceptionResolver methodCommandExceptionResolver = new MethodCommandExceptionResolver();
		methodCommandExceptionResolver.bean = bean;
		methodCommandExceptionResolver.exceptionResolverMethodResolver = exceptionResolverMethodResolver;
		builder.withErrorHandling().resolver(methodCommandExceptionResolver);

		CommandRegistration registration = builder.build();
		registrations.add(registration);
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
