/*
 * Copyright 2022 the original author or authors.
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
package org.springframework.shell.command.annotation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.ApplicationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;
import org.springframework.shell.Availability;
import org.springframework.shell.Utils;
import org.springframework.shell.command.CommandExceptionResolver;
import org.springframework.shell.command.CommandHandlingResult;
import org.springframework.shell.command.CommandRegistration;
import org.springframework.shell.command.CommandRegistration.Builder;
import org.springframework.shell.command.CommandRegistration.OptionArity;
import org.springframework.shell.command.CommandRegistration.OptionSpec;
import org.springframework.shell.command.invocation.InvocableShellMethod;
import org.springframework.shell.completion.CompletionProvider;
import org.springframework.shell.completion.CompletionResolver;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.ReflectionUtils.MethodFilter;

/**
 * Resolves {@link CommandRegistration} using annotations.
 *
 * @author Janne Valkealahti
 */
public class AnnotatedCommandResolver {

	private final Logger log = LoggerFactory.getLogger(AnnotatedCommandResolver.class);
	private final ApplicationContext applicationContext;
	private final Supplier<CommandRegistration.Builder> commandRegistrationBuilderSupplier;
	private static final MethodFilter COMMAND_METHODS = method ->
			AnnotatedElementUtils.hasAnnotation(method, Command.class);

	public AnnotatedCommandResolver(ApplicationContext applicationContext,
			Supplier<CommandRegistration.Builder> commandRegistrationBuilderSupplier) {
		this.applicationContext = applicationContext;
		this.commandRegistrationBuilderSupplier = commandRegistrationBuilderSupplier;
	}

	public Collection<CommandRegistration> resolve() {
		List<CommandRegistration> registrations = new ArrayList<>();
		Map<String, Object> commandBeans = applicationContext.getBeansWithAnnotation(CommandComponent.class);
		log.debug("Found @CommandComponent candidates {}", commandBeans);
		commandBeans.values().forEach(bean -> onCommandComponent(registrations, bean));
		return registrations;
	}

	private void onCommandComponent(List<CommandRegistration> registrations, Object bean) {
		Class<?> clazz = bean.getClass();
		Set<Method> methods = MethodIntrospector.selectMethods(clazz, COMMAND_METHODS);
		methods.forEach(m -> onCommand(registrations, m, bean, clazz));
	}

	private void onCommand(List<CommandRegistration> registrations, Method method, Object bean, Class<?> clazz) {
		Command ann = AnnotatedElementUtils.findMergedAnnotation(method, Command.class);

		Builder builder = commandRegistrationBuilderSupplier.get();

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
					CompletionProvider cr = this.applicationContext.getBean(ovAnn.ref(), CompletionProvider.class);
					if (cr != null) {
						optionSpec.completion(ctx -> cr.apply(ctx));
					}
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

	private String getOrInferGroup(Method method) {
		// ShellMethod methodAnn = AnnotationUtils.getAnnotation(method, ShellMethod.class);
		// if (!methodAnn.group().equals(ShellMethod.INHERITED)) {
		// 	return methodAnn.group();
		// }
		Class<?> clazz = method.getDeclaringClass();
		// ShellCommandGroup classAnn = AnnotationUtils.getAnnotation(clazz, ShellCommandGroup.class);
		// if (classAnn != null && !classAnn.value().equals(ShellCommandGroup.INHERIT_AND_INFER)) {
		// 	return classAnn.value();
		// }
		// ShellCommandGroup packageAnn = AnnotationUtils.getAnnotation(clazz.getPackage(), ShellCommandGroup.class);
		// if (packageAnn != null && !packageAnn.value().equals(ShellCommandGroup.INHERIT_AND_INFER)) {
		// 	return packageAnn.value();
		// }
		// Shameful copy/paste from https://stackoverflow.com/questions/7593969/regex-to-split-camelcase-or-titlecase-advanced
		return StringUtils.arrayToDelimitedString(clazz.getSimpleName().split("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])"), " ");
	}

	// private Supplier<Availability> findAvailabilityIndicator(String[] commandKeys, Object bean, Method method) {
	// 	ShellMethodAvailability explicit = method.getAnnotation(ShellMethodAvailability.class);
	// 	final Method indicator;
	// 	if (explicit != null) {
	// 		Assert.isTrue(explicit.value().length == 1, "When set on a @" +
	// 				ShellMethod.class.getSimpleName() + " method, the value of the @"
	// 				+ ShellMethodAvailability.class.getSimpleName() +
	// 				" should be a single element, the name of a method that returns "
	// 				+ Availability.class.getSimpleName() +
	// 				". Found " + Arrays.asList(explicit.value()) + " for " + method);
	// 		indicator = ReflectionUtils.findMethod(bean.getClass(), explicit.value()[0]);
	// 	} // Try "<method>Availability"
	// 	else {
	// 		Method implicit = ReflectionUtils.findMethod(bean.getClass(), method.getName() + "Availability");
	// 		if (implicit != null) {
	// 			indicator = implicit;
	// 		} else {
	// 			Map<Method, Collection<String>> candidates = new HashMap<>();
	// 			ReflectionUtils.doWithMethods(bean.getClass(), candidate -> {
	// 				List<String> matchKeys = new ArrayList<>(Arrays.asList(candidate.getAnnotation(ShellMethodAvailability.class).value()));
	// 				if (matchKeys.contains("*")) {
	// 					Assert.isTrue(matchKeys.size() == 1, "When using '*' as a wildcard for " +
	// 							ShellMethodAvailability.class.getSimpleName() + ", this can be the only value. Found " +
	// 							matchKeys + " on method " + candidate);
	// 					candidates.put(candidate, matchKeys);
	// 				} else {
	// 					matchKeys.retainAll(Arrays.asList(commandKeys));
	// 					if (!matchKeys.isEmpty()) {
	// 						candidates.put(candidate, matchKeys);
	// 					}
	// 				}
	// 			}, m -> m.getAnnotation(ShellMethodAvailability.class) != null && m.getAnnotation(ShellMethod.class) == null);

	// 			// Make sure wildcard approach has less precedence than explicit name
	// 			Set<Method> notUsingWildcard = candidates.entrySet().stream()
	// 					.filter(e -> !e.getValue().contains("*"))
	// 					.map(Map.Entry::getKey)
	// 					.collect(Collectors.toSet());

	// 			Assert.isTrue(notUsingWildcard.size() <= 1,
	// 					"Found several @" + ShellMethodAvailability.class.getSimpleName() +
	// 							" annotated methods that could apply for " + method + ". Offending candidates are "
	// 							+ notUsingWildcard);

	// 			if (notUsingWildcard.size() == 1) {
	// 				indicator = notUsingWildcard.iterator().next();
	// 			} // Wildcard was available
	// 			else if (candidates.size() == 1) {
	// 				indicator = candidates.keySet().iterator().next();
	// 			} else {
	// 				indicator = null;
	// 			}
	// 		}
	// 	}

	// 	if (indicator != null) {
	// 		Assert.isTrue(indicator.getReturnType().equals(Availability.class),
	// 				"Method " + indicator + " should return " + Availability.class.getSimpleName());
	// 		Assert.isTrue(indicator.getParameterCount() == 0, "Method " + indicator + " should be a no-arg method");
	// 		ReflectionUtils.makeAccessible(indicator);
	// 		return () -> (Availability) ReflectionUtils.invokeMethod(indicator, bean);
	// 	}
	// 	else {
	// 		return null;
	// 	}
	// }
}
