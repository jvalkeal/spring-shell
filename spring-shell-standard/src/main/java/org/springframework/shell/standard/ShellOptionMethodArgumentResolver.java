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
package org.springframework.shell.standard;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.support.NativeMessageHeaderAccessor;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Resolver for {@link ShellOption @ShellOption} arguments. ShellOptions are resolved from
 * either the top-level header map or the nested
 * {@link NativeMessageHeaderAccessor native} header map.
 *
 * @author Janne Valkealahti
 */
public class ShellOptionMethodArgumentResolver extends AbstractNamedValueMethodArgumentResolver {

	private static final Logger logger = LoggerFactory.getLogger(ShellOptionMethodArgumentResolver.class);


	public ShellOptionMethodArgumentResolver(
			ConversionService conversionService, @Nullable ConfigurableBeanFactory beanFactory) {

		super(conversionService, beanFactory);
	}


	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(ShellOption.class);
	}

	@Override
	protected NamedValueInfo createNamedValueInfo(MethodParameter parameter) {
		ShellOption annot = parameter.getParameterAnnotation(ShellOption.class);
		Assert.state(annot != null, "No ShellOption annotation");
		List<String> names = Arrays.stream(annot.value()).map(v -> StringUtils.trimLeadingCharacter(v, '-')).collect(Collectors.toList());
		return new HeaderNamedValueInfo(annot, names);
	}

	@Override
	@Nullable
	protected Object resolveArgumentInternal(MethodParameter parameter, Message<?> message, List<String> names)
			throws Exception {
		Object headerValue = null;
		Object nativeHeaderValue = null;
		for (String name : names) {
			headerValue = message.getHeaders().get(name);
			if (headerValue != null) {
				nativeHeaderValue = getNativeHeaderValue(message, name);
				break;
			}
		}

		if (headerValue != null && nativeHeaderValue != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("A value was found for '" + names + "', in both the top level header map " +
						"and also in the nested map for native headers. Using the value from top level map. " +
						"Use 'nativeHeader.myHeader' to resolve the native header.");
			}
		}

		return (headerValue != null ? headerValue : nativeHeaderValue);
	}

	@Nullable
	private Object getNativeHeaderValue(Message<?> message, String name) {
		Map<String, List<String>> nativeHeaders = getNativeHeaders(message);
		if (name.startsWith("nativeHeaders.")) {
			name = name.substring("nativeHeaders.".length());
		}
		if (nativeHeaders == null || !nativeHeaders.containsKey(name)) {
			return null;
		}
		List<?> nativeHeaderValues = nativeHeaders.get(name);
		return (nativeHeaderValues.size() == 1 ? nativeHeaderValues.get(0) : nativeHeaderValues);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	private Map<String, List<String>> getNativeHeaders(Message<?> message) {
		return (Map<String, List<String>>) message.getHeaders().get(NativeMessageHeaderAccessor.NATIVE_HEADERS);
	}

	@Override
	protected void handleMissingValue(List<String> headerName, MethodParameter parameter, Message<?> message) {
		throw new MessageHandlingException(message, "Missing header '" + headerName +
				"' for method parameter type [" + parameter.getParameterType() + "]");
	}

	private static final class HeaderNamedValueInfo extends NamedValueInfo {

		private HeaderNamedValueInfo(ShellOption annotation, List<String> names) {
			super(names, false, null);
		}
	}
}
