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

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

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

		private final Set<CommandRegistration> commandRegistrations = new CopyOnWriteArraySet<>();

		@Override
		public void register(CommandRegistration registration) {
			commandRegistrations.add(registration);
		}

		@Override
		public Collection<CommandRegistration> getCommands() {
			return commandRegistrations;
		}
	}
}
