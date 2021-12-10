/*
 * Copyright 2015 the original author or authors.
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

package org.springframework.shell;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.core.MethodParameter;

import jakarta.validation.metadata.ElementDescriptor;

// import javax.validation.metadata.ElementDescriptor;

/**
 * Encapsulates information about a shell invokable method parameter, so that it can be documented.
 *
 * <p>Instances of this class are constructed by {@link ParameterResolver#describe(MethodParameter)}.</p>
 *
 * @author Eric Bottard
 */
public class ParameterDescription {

	/**
	 * The original method parameter this is describing.
	 */
	private final MethodParameter parameter;

	/**
	 * A string representation of the type of the parameter.
	 */
	private final String type;

	/**
	 * A string representation of the parameter, as it should appear in a parameter list.
	 * If not provided, this is derived from the parameter type.
	 */
	private String formal;

	/**
	 * A string representation of the default value (if the option is left out entirely) for the parameter, if any.
	 */
	private Optional<String> defaultValue = Optional.empty();

	/**
	 * A string representation of the default value for this parameter, if it can be used as a mere flag (<em>e.g.</em>
	 * {@literal --force} without a value, being an equivalent to {@literal --force true}).
	 * <p>{@literal Optional.empty()} (the default) means that this parameter cannot be used as a flag.</p>
	 */
	private Optional<String> defaultValueWhenFlag = Optional.empty();

	/**
	 * The list of 'keys' that can be used to specify this parameter, if any.
	 */
	private List<String> keys = Collections.emptyList();

	/**
	 * Depending on the {@link ParameterResolver}, whether keys are mandatory to identify this parameter.
	 */
	private boolean mandatoryKey = true;

	/**
	 * A short description of this parameter.
	 */
	private String help = "";

	/**
	 * Allows discovery of bean validation constraints for the command parameter.
	 * <p>Note that most often, constraints will directly come from parameter constraints,
	 * but sometimes (<em>e.g.</em> in case of one method argument mapping to multiple
	 * command options) may come from property constraints.</p>
	 */
	private ElementDescriptor elementDescriptor;

	public ParameterDescription(MethodParameter parameter, String type) {
		this.parameter = parameter;
		this.type = type;
		this.formal = type;
	}

	public static ParameterDescription outOf(MethodParameter parameter) {
		Class<?> type = parameter.getParameterType();
		return new ParameterDescription(parameter, Utils.unCamelify(type.getSimpleName()));
	}

	public ParameterDescription help(String help) {
		this.help = help;
		return this;
	}

	public boolean mandatoryKey() {
		return mandatoryKey;
	}

	public List<String> keys() {
		return keys;
	}

	public Optional<String> defaultValue() {
		return defaultValue;
	}

	public ParameterDescription defaultValue(String defaultValue) {
		this.defaultValue = Optional.ofNullable(defaultValue);
		return this;
	}

	public ParameterDescription whenFlag(String defaultValue) {
		this.defaultValueWhenFlag = Optional.of(defaultValue);
		return this;
	}

	public ParameterDescription keys(List<String> keys) {
		this.keys = keys;
		return this;
	}

	public ParameterDescription mandatoryKey(boolean mandatoryKey) {
		this.mandatoryKey = mandatoryKey;
		return this;
	}

	/**
	 * @return an ElementDescriptor used to discover constraints. May be {@literal null}.
	 */
	public ElementDescriptor elementDescriptor() {
		return this.elementDescriptor;
	}

	public ParameterDescription elementDescriptor(ElementDescriptor descriptor) {
		this.elementDescriptor = descriptor;
		return this;
	}

	public String type() {
		return type;
	}

	public String formal() {
		return formal;
	}

	public String help() {
		return help;
	}

	public Optional<String> defaultValueWhenFlag() {
		return defaultValueWhenFlag;
	}

	public ParameterDescription formal(String formal) {
		this.formal = formal;
		return this;
	}

	@Override
	public String toString() {
		return String.format("%s %s", keys.isEmpty() ? "" : keys().iterator().next(), formal());
	}

	public MethodParameter parameter() {
		return parameter;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ParameterDescription that = (ParameterDescription) o;
		return mandatoryKey == that.mandatoryKey &&
			Objects.equals(parameter, that.parameter) &&
			Objects.equals(type, that.type) &&
			Objects.equals(formal, that.formal) &&
			Objects.equals(defaultValue, that.defaultValue) &&
			Objects.equals(defaultValueWhenFlag, that.defaultValueWhenFlag) &&
			Objects.equals(keys, that.keys) &&
			Objects.equals(help, that.help);
	}

	@Override
	public int hashCode() {
		return Objects.hash(parameter, type, formal, defaultValue, defaultValueWhenFlag, keys, mandatoryKey, help);
	}
}
