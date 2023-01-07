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

/**
 * Annotation marking a method parameter to be a candicate for an option.
 *
 * @author Janne Valkealahti
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Documented
@Reflective
public @interface Option {

	/**
	 * Long names of an option.
	 *
	 * @return Long names of an option
	 */
	String[] longNames() default {};

	/**
	 * Short names of an option.
	 *
	 * @return Short names of an option
	 */
	char[] shortNames() default {};

	/**
	 * Return a short description of the option.
	 *
	 * @return description of the option
	 */
	String description() default "";

	int arityMin() default -1;
	int arityMax() default -1;
	String defaultValue() default "";
}
