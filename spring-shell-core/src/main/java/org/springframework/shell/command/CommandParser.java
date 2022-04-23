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
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
		 * Gets the unmapped positional arguments.
		 *
		 * @return the unmapped positional arguments
		 */
		List<String> positional();

		/**
		 * Gets parsing errors.
		 *
		 * @return the parsing errors
		 */
		List<CommandParserException> errors();

		/**
		 * Gets an instance of a default {@link Results}.
		 *
		 * @param results the results
		 * @param positional the list of positional arguments
		 * @param errors the parsing errors
		 * @return a new instance of results
		 */
		static Results of(List<Result> results, List<String> positional, List<CommandParserException> errors) {
			return new DefaultResults(results, positional, errors);
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
		private List<CommandParserException> errors;

		DefaultResults(List<Result> results, List<String> positional, List<CommandParserException> errors) {
			this.results = results;
			this.positional = positional;
			this.errors = errors;
		}

		@Override
		public List<Result> results() {
			return results;
		}

		@Override
		public List<String> positional() {
			return positional;
		}

		@Override
		public List<CommandParserException> errors() {
			return errors;
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
			List<CommandOption> requiredOptions = options.stream()
				.filter(o -> o.isRequired())
				.collect(Collectors.toList());

			Lexer lexer = new Lexer(args);
			List<List<String>> lexerResults = lexer.visit();
			Parser parser = new Parser();
			ParserResults parserResults = parser.visit(lexerResults, options);

			List<Result> results = new ArrayList<>();
			List<String> positional = new ArrayList<>();
			List<CommandParserException> errors = new ArrayList<>();
			parserResults.results.stream().forEach(pr -> {
				if (pr.option != null) {
					results.add(new DefaultResult(pr.option, pr.value));
					requiredOptions.remove(pr.option);
				}
				else {
					positional.addAll(pr.args);
				}
				if (pr.error != null) {
					errors.add(pr.error);
				}
			});

			requiredOptions.stream().forEach(o -> {
				errors.add(MissingOptionException.of("Missing option", o));
			});

			return new DefaultResults(results, positional, errors);
		}

		private static class ParserResult {
			private CommandOption option;
			private List<String> args;
			private Object value;
			private CommandParserException error;

			private ParserResult(CommandOption option, List<String> args, Object value, CommandParserException error) {
				this.option = option;
				this.args = args;
				this.value = value;
				this.error = error;
			}

			static ParserResult of(CommandOption option, List<String> args, Object value,
					CommandParserException error) {
				return new ParserResult(option, args, value, error);
			}
		}

		private static class ParserResults {
			private List<ParserResult> results;

			private ParserResults(List<ParserResult> results) {
				this.results = results;
			}

			static ParserResults of(List<ParserResult> results) {
				return new ParserResults(results);
			}
 		}

		/**
		 * Parser works on a results from a lexer. It looks for given options
		 * and builds parsing results.
		 */
		private static class Parser {
			ParserResults visit(List<List<String>> lexerResults, List<CommandOption> options) {
				List<ParserResult> results = lexerResults.stream()
					.flatMap(lr -> {
						List<CommandOption> option = matchOptions(options, lr.get(0));
						if (option.isEmpty()) {
							return Stream.of(ParserResult.of(null, lr, null, null));
						}
						else {
							return option.stream().map(o -> {
								List<String> subArgs = lr.subList(1, lr.size());
								Object value = convertArguments(o, subArgs);
								return ParserResult.of(o, subArgs, value, null);
							});
						}
					})
					.collect(Collectors.toList());
				return ParserResults.of(results);
			}

			private List<CommandOption> matchOptions(List<CommandOption> options, String arg) {
				List<CommandOption> matched = new ArrayList<>();
				String trimmed = StringUtils.trimLeadingCharacter(arg, '-');
				int count = arg.length() - trimmed.length();
				if (count == 1) {
					if (trimmed.length() == 1) {
						Character trimmedChar = trimmed.charAt(0);
						options.stream()
							.filter(o -> {
								for (Character sn : o.getShortNames()) {
									if (trimmedChar.equals(sn)) {
										return true;
									}
								}
								return false;
							})
						.findFirst()
						.ifPresent(o -> matched.add(o));
					}
					else if (trimmed.length() > 1) {
						trimmed.chars().mapToObj(i -> (char)i)
							.forEach(c -> {
								options.stream().forEach(o -> {
									for (Character sn : o.getShortNames()) {
										if (c.equals(sn)) {
											matched.add(o);
										}
									}
								});
							});
					}
				}
				else if (count == 2) {
					options.stream()
						.filter(o -> {
							for (String ln : o.getLongNames()) {
								if (trimmed.equals(ln)) {
									return true;
								}
							}
							return false;
						})
						.findFirst()
						.ifPresent(o -> matched.add(o));
				}
				return matched;
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
					if (arguments.isEmpty()) {
						return null;
					}
					else {
						return arguments.stream().collect(Collectors.joining(" "));
					}
				}
			}
		}

		/**
		 * Lexers only responsibility is to splice arguments array into
		 * chunks which belongs together what comes for option structure.
		 */
		private static class Lexer {
			private final String[] args;
			Lexer(String[] args) {
				this.args = args;
			}
			List<List<String>> visit() {
				List<List<String>> results = new ArrayList<>();

				List<String> splice = null;
				for (int i = 0; i < args.length; i++) {
					if (splice == null) {
						splice = new ArrayList<>();
					}
					String arg = args[i];
					boolean isOption = arg.startsWith("-");
					if (isOption) {
						if (splice.isEmpty()) {
							splice.add(arg);
						}
						else {
							results.add(splice);
							splice = null;
							splice = new ArrayList<>();
							splice.add(arg);
						}
					}
					else {
						splice.add(arg);
					}
				}
				if (splice != null && !splice.isEmpty()) {
					results.add(splice);
				}
				return results;
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

		private CommandOption option;

		public MissingOptionException(String message, CommandOption option) {
			super(message);
			this.option = option;
		}

		public static MissingOptionException of(String message, CommandOption option) {
			return new MissingOptionException(message, option);
		}

		public CommandOption getOption() {
			return option;
		}
	}
}
