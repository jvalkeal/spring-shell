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

import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.shell.command.annotation.Command;

/**
 * Utilities to merge {@link Command} annotations using opinionated logic.
 *
 * @author Janne Valkealahti
 */
class CommandAnnotationUtils {

	/**
	 * Get a boolean field where right side overrides if it's defined.
	 *
	 * @param name the field name
	 * @param left the left side annotation
	 * @param right the right side annotation
	 * @return resolved boolean
	 */
	static boolean resolveBoolean(String name, MergedAnnotation<?> left, MergedAnnotation<?> right) {
		Boolean def = right.getDefaultValue(name, Boolean.class).orElse(null);

		boolean l = left.getBoolean(name);
		boolean r = right.getBoolean(name);

		if (def != null) {
			if (def != r) {
				l = r;
			}
		}
		else {
			l = r;
		}

		return l;
	}

}
