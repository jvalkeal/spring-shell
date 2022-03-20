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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.core.ResolvableType;
import org.springframework.util.ObjectUtils;

/**
 * Interface parsing arguments for a {@link CommandRegistration}. A command is
 * always identified by a set of words like
 * {@code command subcommand1 subcommand2} and remaining part of it are options
 * which this interface intercepts and translates into format we can understand.
 *
 * @author Janne Valkealahti
 */
public interface CommandParser {

	/**
	 * Result of a parsing {@link CommandOption} with an argument.
	 */
	interface Result {

		/**
		 * Gets the {@link CommandOption}.
		 *
		 * @return the command option
		 */
		CommandOption option();

		/**
		 * Gets the value.
		 *
		 * @return the value
		 */
		Object value();

		/**
		 * Gets an instance of a default {@link Result}.
		 *
		 * @param option the command option
		 * @param value the value
		 * @return a result
		 */
		static Result of(CommandOption option, Object value) {
			return new DefaultResult(option, value);
		}
	}

	/**
	 * Results of a {@link CommandParser}. Basically contains a list of {@link Result}s.
	 */
	interface Results {

		/**
		 * Gets the results.
		 *
		 * @return the results
		 */
		List<Result> results();

		/**
		 * Gets an instance of a default {@link Results}.
		 *
		 * @param results
		 * @return
		 */
		static Results of(List<Result> results) {
			return new DefaultResults(results);
		}
	}

	/**
	 * Parse options with a given arguments.
	 *
	 * @param options the command options
	 * @param args the arguments
	 * @return parsed results
	 */
	Results parse(List<CommandOption> options, String[] args);

	/**
	 * Gets an instance of a default command parser.
	 *
	 * @return instance of a default command parser
	 */
	static CommandParser of() {
		return new DefaultCommadParser();
	}

	/**
	 * Default implementation of a {@link Results}.
	 */
	static class DefaultResults implements Results {

		private List<Result> results;

		DefaultResults(List<Result> results) {
			this.results = results;
		}

		@Override
		public List<Result> results() {
			return results;
		}
	}

	/**
	 * Default implementation of a {@link Result}.
	 */
	static class DefaultResult implements Result {

		private CommandOption option;
		private Object value;

		DefaultResult(CommandOption option, Object value) {
			this.option = option;
			this.value = value;
		}

		@Override
		public CommandOption option() {
			return option;
		}

		@Override
		public Object value() {
			return value;
		}
	}

	/**
	 * Default implementation of a {@link CommandParser}.
	 */
	static class DefaultCommadParser implements CommandParser {

		@Override
		public Results parse(List<CommandOption> options, String[] args) {
			// Currently relatively naive way to parse options and is expected to
			// get a full rework when more sophisticated features like options groups
			// etc are added.
			List<Result> results = new ArrayList<>();
			CommandOption onOption = null;

			for (int i = 0; i < args.length; i++) {
				String arg = args[i];
				if (onOption == null) {
					Optional<CommandOption> option = options.stream()
						.filter(o -> ObjectUtils.containsElement(o.getAliases(), arg))
						.findFirst();
					if (option.isPresent()) {
						onOption = option.get();
					}
				}
				else {
					// ResolvableType type = onOption.getType();
					results.add(Result.of(onOption, arg));
					onOption = null;
				}
			}
			return Results.of(results);
		}
	}
}
