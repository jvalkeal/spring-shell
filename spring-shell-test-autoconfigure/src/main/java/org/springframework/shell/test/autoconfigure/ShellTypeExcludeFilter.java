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
package org.springframework.shell.test.autoconfigure;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.boot.test.autoconfigure.filter.StandardAnnotationCustomizableTypeExcludeFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * {@link TypeExcludeFilter} for {@link ShellTest @ShellTest}.
 *
 * @author Janne Valkealahti
 */
public class ShellTypeExcludeFilter extends StandardAnnotationCustomizableTypeExcludeFilter<ShellTest> {

	private static final Class<?>[] NO_CONTROLLERS = {};

	private static final String[] OPTIONAL_INCLUDES = {};

	private static final Set<Class<?>> DEFAULT_INCLUDES;

	static {
		Set<Class<?>> includes = new LinkedHashSet<>();
		for (String optionalInclude : OPTIONAL_INCLUDES) {
			try {
				includes.add(ClassUtils.forName(optionalInclude, null));
			}
			catch (Exception ex) {
				// Ignore
			}
		}
		DEFAULT_INCLUDES = Collections.unmodifiableSet(includes);
	}

	private static final Set<Class<?>> DEFAULT_INCLUDES_AND_CONTROLLER;

	static {
		Set<Class<?>> includes = new LinkedHashSet<>(DEFAULT_INCLUDES);
		includes.add(ShellComponent.class);
		// includes.add(Configuration.class);
		DEFAULT_INCLUDES_AND_CONTROLLER = Collections.unmodifiableSet(includes);
	}

	private final Class<?>[] controllers;

	ShellTypeExcludeFilter(Class<?> testClass) {
		super(testClass);
		this.controllers = getAnnotation().getValue("controllers", Class[].class).orElse(NO_CONTROLLERS);
	}

	@Override
	protected Set<Class<?>> getDefaultIncludes() {
		if (ObjectUtils.isEmpty(this.controllers)) {
			return DEFAULT_INCLUDES_AND_CONTROLLER;
		}
		return DEFAULT_INCLUDES;
	}

	@Override
	protected Set<Class<?>> getComponentIncludes() {
		return new LinkedHashSet<>(Arrays.asList(this.controllers));
	}
}
