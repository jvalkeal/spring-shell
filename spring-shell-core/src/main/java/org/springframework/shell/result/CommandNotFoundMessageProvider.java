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
package org.springframework.shell.result;

import java.util.Map;
import java.util.function.Function;

import org.springframework.shell.command.CommandRegistration;
import org.springframework.shell.result.CommandNotFoundMessageProvider.ProviderContext;

/**
 *
 *
 * @author Janne Valkealahti
 */
public interface CommandNotFoundMessageProvider extends Function<ProviderContext, String> {

	// spring.shell.error.command.notfound.foo=bar
	// spring.shell.error.command.notfound.type=bar
	// spring.shell.error.command.notfound.template=path

	static ProviderContext contextOf(Map<String, CommandRegistration> registrations, String text) {
		return new ProviderContext() {

			@Override
			public Map<String, CommandRegistration> registrations() {
				return registrations;
			}

			@Override
			public String text() {
				return text;
			}
		};
	}

	interface ProviderContext {
		Map<String, CommandRegistration> registrations();
		String text();
	}

}
