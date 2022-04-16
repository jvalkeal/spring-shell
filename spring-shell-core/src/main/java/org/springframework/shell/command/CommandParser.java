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
import java.util.stream.Collectors;

import org.springframework.core.ResolvableType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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
		 * Gets the unmapped positional arguments.
		 *
		 * @return the unmapped positional arguments
		 */
		List<String> positional();

		/**
		 * Gets an instance of a default {@link Results}.
		 *
		 * @param results the results
		 * @param positional the list of positional arguments
		 * @return a new instance of results
		 */
		static Results of(List<Result> results, List<String> positional) {
			return new DefaultResults(results, positional);
		}
	}

	/**
	 * Parse options with a given arguments.
	 *
	 * May throw various runtime exceptions depending how parser is configure.
	 * For example if required option is missing an exception is thrown.
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
		private List<String> positional;

		DefaultResults(List<Result> results, List<String> positional) {
			this.results = results;
			this.positional = positional;
		}

		@Override
		public List<Result> results() {
			return results;
		}

		@Override
		public List<String> positional() {
			return positional;
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
			VisitResults visitResults = argsVisitor.visitArgs();
			return Results.of(visitResults.results, visitResults.positional);
		}

		private static enum State {
			EXPECT_OPTION, EXPECT_ARGUMENT, ON_ARGUMENTS
		}

		private static class VisitResults {
			List<Result> results;
			List<String> positional;

			VisitResults(List<Result> results, List<String> positional) {
				this.results = results;
				this.positional = positional;
			}

			static VisitResults of(List<Result> results, List<String> positional) {
				return new VisitResults(results, positional);
			}
		}

		private static class ArgsVisitor {
			final List<CommandOption> options;
			final String[] args;
			final List<CommandOption> optionsRequired;
			int position;
			State state = State.EXPECT_OPTION;
			CommandOption currentOption;
			MultiValueMap<CommandOption, String> mappedArguments = new LinkedMultiValueMap<>();
			List<String> positional = new ArrayList<>();

			ArgsVisitor(List<CommandOption> options, String[] args) {
				this.options = options;
				this.args = args;
				this.optionsRequired = options.stream().filter(o -> o.isRequired()).collect(Collectors.toList());
			}

			private VisitResults visitArgs() {
				List<Result> results = new ArrayList<>();

				// first iteration to map arguments to options
				for (int i = 0; i < args.length; i++) {
					position = i;
					visitArg();
				}

				// second iteration for mapped argument creating requested types
				mappedArguments.entrySet().stream().forEach(e -> {
					CommandOption option = e.getKey();
					List<String> arguments = e.getValue();
					results.add(Result.of(option, convertArguments(option, arguments)));
				});

				// may throw errors
				sanityCheck();
				return VisitResults.of(results, positional);
			}

			private void visitArg() {
				String arg = args[position];
				if (state == State.EXPECT_OPTION) {
					CommandOption option = matchOption(arg);
					if (option != null) {
						currentOption = option;
						state = State.EXPECT_ARGUMENT;
						onOptionFound(currentOption);
					}
					else {
						// stash unmapped arguments
						positional.add(arg);
					}
				}
				else if (state == State.EXPECT_ARGUMENT) {
					mappedArguments.add(currentOption, arg);
					state = State.ON_ARGUMENTS;
				}
				else if (state == State.ON_ARGUMENTS) {
					CommandOption option = matchOption(arg);
					if (option != null) {
						currentOption = option;
						state = State.EXPECT_ARGUMENT;
					}
					else {
						mappedArguments.add(currentOption, arg);
					}
				}
			}

			private CommandOption matchOption(String arg) {
				Optional<CommandOption> option = options.stream()
					.filter(o -> optionMatchesArg(o, arg))
					.findFirst();
				return option.orElse(null);
			}

			private void onOptionFound(CommandOption option) {
				mappedArguments.putIfAbsent(option, new ArrayList<>());
				optionsRequired.remove(option);
			}

			private void sanityCheck() {
				if (!optionsRequired.isEmpty()) {
					throw MissingOptionException.of("Missing required options", optionsRequired);
				}
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

			private Object convertArguments(CommandOption option, List<String> arguments) {
				ResolvableType type = option.getType();
				if (type != null && type.isAssignableFrom(boolean.class)) {
					if (arguments.size() == 0) {
						return true;
					}
					else {
						return Boolean.parseBoolean(arguments.get(0));
					}
				}
				else {
					return arguments.stream().collect(Collectors.joining(" "));
				}
			}
		}
	}

	static class CommandParserException extends RuntimeException {

		public CommandParserException(String message) {
			super(message);
		}

		public CommandParserException(String message, Throwable cause) {
			super(message, cause);
		}

		public static CommandParserException of(String message) {
			return new CommandParserException(message);
		}
	}

	static class MissingOptionException extends CommandParserException {

		private List<CommandOption> options;

		public MissingOptionException(String message, List<CommandOption> options) {
			super(message);
			this.options = options;
		}

		public static MissingOptionException of(String message, List<CommandOption> options) {
			return new MissingOptionException(message, options);
		}

		public List<CommandOption> getOptions() {
			return options;
		}
	}
}
