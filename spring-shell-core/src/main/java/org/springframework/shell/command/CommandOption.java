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

/**
 * Interface representing an option in a command.
 *
 * @author Janne Valkealahti
 */
public interface CommandOption {

	/**
	 * Gets a long names of an option.
	 *
	 * @return long names of an option
	 */
	String[] getLongNames();

	/**
	 * Gets a short names of an option.
	 *
	 * @return short names of an option
	 */
	Character[] getShortNames();

	/**
	 * Gets a description of an option.
	 *
	 * @return description of an option
	 */
	String getDescription();

	/**
	 * Gets an instance of a default {@link CommandOption}.
	 *
	 * @param longNames the long names
	 * @param shortNames the short names
	 * @param description the description
	 * @return default command option
	 */
	public static CommandOption of(String[] longNames, Character[] shortNames, String description) {
		return new DefaultCommandOption(longNames, shortNames, description);
	}

	/**
	 * Default implementation of {@link CommandOption}.
	 */
	public static class DefaultCommandOption implements CommandOption {

		private String[] longNames;
		private Character[] shortNames;
		private String description;

		public DefaultCommandOption(String[] longNames, Character[] shortNames, String description) {
			this.longNames = longNames;
			this.shortNames = shortNames;
			this.description = description;
		}

		@Override
		public String[] getLongNames() {
			return longNames;
		}

		@Override
		public Character[] getShortNames() {
			return shortNames;
		}

		@Override
		public String getDescription() {
			return description;
		}
	}
}
