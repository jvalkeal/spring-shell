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
package org.springframework.shell.xxx;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.util.StringUtils;

/**
 *
 *
 * @author Janne Valkealahti
 */
public interface CommandCatalog {

	/**
	 * Register a {@link CommandRegistration}.
	 *
	 * @param registration the command registration
	 */
	void register(CommandRegistration registration);

	/**
	 * Unregister a {@link CommandRegistration}.
	 *
	 * @param registration the command registration
	 */
	void unregister(CommandRegistration registration);

	/**
	 * Gets all command registrations.
	 *
	 * @return command registrations
	 */
	Collection<CommandRegistration> getCommands();

	/**
	 * Gets an instance of a default {@link CommandCatalog}.
	 *
	 * @return default command catalog
	 */
	static CommandCatalog of() {
		return new DefaultCommandCatalog();
	}

	/**
	 * Default implementation of a {@link CommandCatalog}.
	 */
	static class DefaultCommandCatalog implements CommandCatalog {

		private final HashMap<String, CommandRegistration> commandRegistrations = new HashMap<>();

		@Override
		public void register(CommandRegistration registration) {
			String commandName = commandName(registration.getCommands());
			commandRegistrations.put(commandName, registration);
		}

		@Override
		public void unregister(CommandRegistration registration) {
			String commandName = commandName(registration.getCommands());
			commandRegistrations.remove(commandName);
		}

		@Override
		public Collection<CommandRegistration> getCommands() {
			return commandRegistrations.values();
		}

		private static String commandName(String[] commands) {
			return Arrays.asList(commands).stream()
				.flatMap(c -> Stream.of(c.split(" ")))
				.filter(c -> StringUtils.hasText(c))
				.map(c -> c.trim())
				.collect(Collectors.joining(" "));
		}
	}
}
