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
import org.springframework.util.StringUtils;

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
		return new DefaultCommandParser();
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
	static class DefaultCommandParser implements CommandParser {

		@Override
		public Results parse(List<CommandOption> options, String[] args) {
			ArgsVisitor argsVisitor = new ArgsVisitor(options, args);
			List<Result> results = argsVisitor.visit();
			return Results.of(results);
		}

		private static class ArgsVisitor {
			final List<CommandOption> options;
			final String[] args;
			int position;

			ArgsVisitor(List<CommandOption> options, String[] args) {
				this.options = options;
				this.args = args;
			}

			List<Result> visit() {
				List<Result> results = new ArrayList<>();
				Result result;
				while ((result = nextResult()) != null) {
					results.add(result);
				}
				return results;
			}

			private Result nextResult() {
				if (position > args.length - 1) {
					return null;
				}
				String arg = args[position];
				Optional<CommandOption> option = options.stream()
					.filter(o -> optionMatchesArg(o, arg))
					.findFirst();
				if (option.isPresent()) {
					String nextArg = position < args.length - 1 ? args[position + 1] : null;
					ResolvableType type = option.get().getType();
					if (type != null && type.isAssignableFrom(boolean.class)) {
						boolean value = nextArg != null ? Boolean.parseBoolean(nextArg) : true;
						position++;
						return Result.of(option.get(), value);
					}
					else {
						if (nextArg != null) {
							position++;
							return Result.of(option.get(), nextArg);
						}
					}
				}
				position++;
				return null;
			}

			private boolean optionMatchesArg(CommandOption option, String arg) {
				if (!arg.startsWith("-")) {
					return false;
				}
				arg = StringUtils.trimLeadingCharacter(arg, '-');
				for (String alias : option.getLongNames()) {
					if (arg.equals(alias)) {
						return true;
					}
				}
				if (arg.length() == 1) {
					for (Character c : option.getShortNames()) {
						if(arg.equals(c.toString())) {
							return true;
						}
					}
				}
				return false;
			}

		}
	}

}
