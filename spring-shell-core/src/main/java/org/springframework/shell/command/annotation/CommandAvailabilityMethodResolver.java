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
