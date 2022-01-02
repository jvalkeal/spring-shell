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
package org.springframework.shell.standard.completion;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.stringtemplate.v4.ST;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public abstract class AbstractCompletions {

	private ResourceLoader resourceLoader;

	public AbstractCompletions(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	protected Builder builder() {
		return new DefaultBuilder();
	}

	private static String resourceAsString(Resource resource) {
		try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
			return FileCopyUtils.copyToString(reader);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	interface Builder {

		Builder withDefaultAttribute(String name, Object value);
		Builder withDefaultMultiAttribute(String name, List<? extends Object> values);
		Builder appendResourceWithRender(String resource);
		String build();
	}

	class DefaultBuilder implements Builder {

		private final MultiValueMap<String, Object> defaultAttributes = new LinkedMultiValueMap<>();
		private final List<Supplier<String>> operations = new ArrayList<>();

		@Override
		public Builder withDefaultAttribute(String name, Object value) {
			this.defaultAttributes.add(name, value);
			return this;
		}

		@Override
		public Builder withDefaultMultiAttribute(String name, List<? extends Object> values) {
			this.defaultAttributes.addAll(name, values);
			return this;
		}

		@Override
		public Builder appendResourceWithRender(String resource) {
			// delay so that we render with build
			Supplier<String> operation = () -> {
				String template = resourceAsString(resourceLoader.getResource(resource));
				ST st = new ST(template);
				defaultAttributes.entrySet().stream().forEach(entry -> {
					String key = entry.getKey();
					List<Object> values = entry.getValue();
					values.stream().forEach(v -> {
						st.add(key, v);
					});
				});
				return st.render();
			};
			operations.add(operation);
			return this;
		}

		@Override
		public String build() {
			StringBuilder buf = new StringBuilder();
			operations.stream().forEach(operation -> {
				buf.append(operation.get());
			});
			return buf.toString();
		}
	}
}
