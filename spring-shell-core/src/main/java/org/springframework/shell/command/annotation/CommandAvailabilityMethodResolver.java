package org.springframework.shell.command.annotation;

import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ReflectionUtils.MethodFilter;

public class CommandAvailabilityMethodResolver {

	public static final MethodFilter COMMAND_AVAILABILITY_METHODS = method ->
			AnnotatedElementUtils.hasAnnotation(method, CommandAvailability.class);

	public CommandAvailabilityMethodResolver(Class<?> handlerType) {
		MethodIntrospector.selectMethods(handlerType, COMMAND_AVAILABILITY_METHODS);
	}
}
