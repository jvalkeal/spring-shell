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
package org.springframework.shell.command;

import org.springframework.core.ResolvableType;

/**
 * Interface representing an option in a command.
 *
 * @author Janne Valkealahti
 */
public interface CommandOption {

	/**
	 * Gets a name of an option.
	 *
	 * @return name of an option
	 */
	String getName();

	/**
	 * Gets an aliases of an option
	 *
	 * @return aliases of an option
	 */
	String[] getAliases();

	/**
	 * Gets a description of an option.
	 *
	 * @return description of an option
	 */
	String getDescription();

	// ResolvableType getType();

	/**
	 * Gets an instance of a default {@link CommandOption}.
	 *
	 * @param name the name
	 * @param aliases the aliases
	 * @param description the description
	 * @return default command option
	 */
	public static CommandOption of(String name, String[] aliases, /*ResolvableType type,*/ String description) {
		return new DefaultCommandOption(name, aliases, /*type,*/ description);
	}

	/**
	 * Default implementation of {@link CommandOption}.
	 */
	public static class DefaultCommandOption implements CommandOption {

		private String name;
		private String[] aliases;
		// private ResolvableType type;
		private String description;

		public DefaultCommandOption(String name, String[] aliases, /*ResolvableType type,*/ String description) {
			this.name = name;
			this.aliases = aliases;
			// this.type = type;
			this.description = description;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String[] getAliases() {
			return aliases;
		}

		// @Override
		// public ResolvableType getType() {
		// 	return type;
		// }

		@Override
		public String getDescription() {
			return description;
		}
	}
}
