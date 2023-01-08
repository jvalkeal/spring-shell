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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.aot.hint.annotation.Reflective;
import org.springframework.shell.context.InteractionMode;

/**
 * Annotation marking a method to be a candicate for a shell command target.
 *
 * @author Janne Valkealahti
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Documented
@Reflective
public @interface Command {

	/**
	 * Command as an array.
	 *
	 * @return the command as an array
	 */
	String[] command() default {};

	/**
	 * Name of a command group.
	 *
	 * @return the command group
	 */
	String group() default "";

	/**
	 * Description of a command.
	 *
	 * @return the Description of a command
	 */
	String description() default "";

	/**
	 * Defines if a command should be hidden.
	 *
	 * @return true if command should be hidden
	 */
	boolean hidden() default false;

	/**
	 * Defines interaction mode for a command as a hint when command should be
	 * available. For example presense of some commands doesn't make sense if shell
	 * is running as non-interactive mode and vice versa.
	 *
	 * Defaults to {@link InteractionMode#ALL}
	 *
	 * @return interaction mode
	 */
	InteractionMode interactionMode() default InteractionMode.ALL;
}
