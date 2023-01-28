/*
 * Copyright 2022-2023 the original author or authors.
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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionService;
import org.springframework.shell.Utils;
import org.springframework.shell.command.parser.Ast;
import org.springframework.shell.command.parser.CommandModel;
import org.springframework.shell.command.parser.ParserConfiguration;
import org.springframework.shell.command.parser.Ast.DefaultAst;
import org.springframework.shell.command.parser.Lexer.DefaultLexer;
import org.springframework.shell.command.parser.Parser.DefaultParser;
import org.springframework.shell.command.parser.Parser.ParseResult;
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
	interface CommandParserResult {

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
		 * Gets an instance of a default {@link CommandParserResult}.
		 *
		 * @param option the command option
		 * @param value the value
		 * @return a result
		 */
		static CommandParserResult of(CommandOption option, Object value) {
			return new DefaultCommandParserResult(option, value);
		}
	}

	/**
	 * Results of a {@link CommandParser}. Basically contains a list of {@link CommandParserResult}s.
	 */
	interface CommandParserResults {

		/**
		 * Gets the results.
		 *
		 * @return the results
		 */
		List<CommandParserResult> results();

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
		 * Gets an instance of a default {@link CommandParserResults}.
		 *
		 * @param results the results
		 * @param positional the list of positional arguments
		 * @param errors the parsing errors
		 * @return a new instance of results
		 */
		static CommandParserResults of(List<CommandParserResult> results, List<String> positional, List<CommandParserException> errors) {
			return new DefaultCommandParserResults(results, positional, errors);
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
	CommandParserResults parse(List<CommandOption> options, String[] args);

	/**
	 * Gets an instance of a default command parser.
	 *
	 * @return instance of a default command parser
	 */
	static CommandParser of() {
		return of(null);
	}

	/**
	 * Gets an instance of a default command parser.
	 *
	 * @param conversionService the conversion service
	 * @return instance of a default command parser
	 */
	static CommandParser of(ConversionService conversionService) {
		return new DefaultCommandParser(conversionService);
	}

	/**
	 * Default implementation of a {@link CommandParserResults}.
	 */
	static class DefaultCommandParserResults implements CommandParserResults {

		private List<CommandParserResult> results;
		private List<String> positional;
		private List<CommandParserException> errors;

		DefaultCommandParserResults(List<CommandParserResult> results, List<String> positional, List<CommandParserException> errors) {
			this.results = results;
			this.positional = positional;
			this.errors = errors;
		}

		@Override
		public List<CommandParserResult> results() {
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
	 * Default implementation of a {@link CommandParserResult}.
	 */
	static class DefaultCommandParserResult implements CommandParserResult {

		private CommandOption option;
		private Object value;

		DefaultCommandParserResult(CommandOption option, Object value) {
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

	static class AstCommandParser implements CommandParser {

		private final Map<String, CommandRegistration> registrations;
		private final ParserConfiguration configuration;
		private final ConversionService conversionService;

		public AstCommandParser(Map<String, CommandRegistration> registrations, ParserConfiguration configuration,
				ConversionService conversionService) {
			this.registrations = registrations;
			this.configuration = configuration;
			this.conversionService = conversionService;
		}

		@Override
		public CommandParserResults parse(List<CommandOption> options, String[] args) {
			CommandModel commandModel = new CommandModel(registrations, configuration);
			org.springframework.shell.command.parser.Lexer lexer = new DefaultLexer(commandModel, configuration);
			Ast ast = new DefaultAst();
			org.springframework.shell.command.parser.Parser parser = new DefaultParser(commandModel, lexer, ast,
					configuration, conversionService);
			ParseResult result = parser.parse(Arrays.asList(args));

			List<CommandParserResult> results = new ArrayList<>();
			List<String> positional = new ArrayList<>();
			List<CommandParserException> errors = new ArrayList<>();

			result.optionResults().forEach(or -> {
				results.add(CommandParserResult.of(or.option(), or.value()));
			});

			return new DefaultCommandParserResults(results, positional, errors);
		}
	}

	/**
	 * Default implementation of a {@link CommandParser}.
	 */
	static class DefaultCommandParser implements CommandParser {

		private final ConversionService conversionService;

		DefaultCommandParser(ConversionService conversionService) {
			this.conversionService = conversionService;
		}

		@Override
		public CommandParserResults parse(List<CommandOption> options, String[] args) {
			List<CommandOption> requiredOptions = options.stream()
				.filter(o -> o.isRequired())
				.collect(Collectors.toList());

			Lexer lexer = new Lexer(args);
			List<List<String>> lexerResults = lexer.visit();
			Parser parser = new Parser();
			ParserResults parserResults = parser.visit(lexerResults, options);

			List<CommandParserResult> results = new ArrayList<>();
			List<String> positional = new ArrayList<>();
			List<CommandParserException> errors = new ArrayList<>();
			parserResults.results.stream().forEach(pr -> {
				if (pr.option != null) {
					results.add(new DefaultCommandParserResult(pr.option, pr.value));
					requiredOptions.remove(pr.option);
				}
				else {
					for (String arg : pr.args) {
						if (arg.startsWith("-")) {
							errors.add(UnrecognisedOptionException.of(String.format("Unrecognised option '%s'", arg),
									arg));
						}
						else {
							positional.add(arg);
						}
					}
				}
				if (pr.error != null) {
					errors.add(pr.error);
				}
			});

			Deque<ParserResult> queue = new ArrayDeque<>(parserResults.results);
			options.stream()
				.filter(o -> o.getPosition() > -1)
				.sorted(Comparator.comparingInt(o -> o.getPosition()))
				.forEach(o -> {
					int arityMin = o.getArityMin();
					int arityMax = o.getArityMax();
					List<String> oargs = new ArrayList<>();
					if (arityMin > -1) {
						for (int i = 0; i < arityMax; i++) {
							ParserResult pop = null;
							if (!queue.isEmpty()) {
								pop = queue.pop();
							}
							else {
								break;
							}
							if (pop != null && pop.option == null) {
								if (!pop.args.isEmpty()) {
									oargs.addAll(pop.args);
								}
							}
						}
					}
					// don't do anything if first arg looks like an option as if we are here
					// then we'd might remove wrong required option
					if (!oargs.isEmpty() && !oargs.get(0).startsWith("-")) {
						// as we now have a candicate option, try to see if there is a
						// conversion we can do and the use it.
						Object value = convertOptionType(o, oargs);
						results.add(new DefaultCommandParserResult(o, value));
						requiredOptions.remove(o);
					}
				});

			requiredOptions.stream().forEach(o -> {
				String ln = o.getLongNames() != null ? Stream.of(o.getLongNames()).collect(Collectors.joining(",")) : "";
				String sn = o.getShortNames() != null ? Stream.of(o.getShortNames()).map(n -> Character.toString(n))
						.collect(Collectors.joining(",")) : "";
				errors.add(MissingOptionException
						.of(String.format("Missing option, longnames='%s', shortnames='%s'", ln, sn), o));
			});

			return new DefaultCommandParserResults(results, positional, errors);
		}

		private Object convertOptionType(CommandOption option, Object value) {
			if (conversionService != null && option.getType() != null && value != null) {
				if (conversionService.canConvert(value.getClass(), option.getType().getRawClass())) {
					value = conversionService.convert(value, option.getType().getRawClass());
				}
			}
			return value;
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
		private class Parser {
			ParserResults visit(List<List<String>> lexerResults, List<CommandOption> options) {
				List<ParserResult> results = lexerResults.stream()
					.flatMap(lr -> {
						List<CommandOption> option = matchOptions(options, lr.get(0));
						if (option.isEmpty()) {
							return lr.stream().map(a -> ParserResult.of(null, Arrays.asList(a), null, null));
						}
						else {
							return option.stream().flatMap(o -> {
								List<String> subArgs = lr.subList(1, lr.size());
								ConvertArgumentsHolder holder = convertArguments(o, subArgs);
								if (holder.error != null) {
									return Stream.of(ParserResult.of(o, subArgs, null, holder.error));
								}
								Object value = convertOptionType(o, holder.value);
								Stream<ParserResult> unmapped = holder.unmapped.stream()
									.map(um -> ParserResult.of(null, Arrays.asList(um), null, null));
								Stream<ParserResult> res = Stream.of(ParserResult.of(o, subArgs, value, null));
								return Stream.concat(res, unmapped);
							});
						}
					})
					.collect(Collectors.toList());

				// Check options which didn't get matched and add parser result
				// for those having default value.
				List<CommandOption> defaultValueOptionsToCheck = new ArrayList<>(options);
				results.stream()
					.forEach(pr -> {
						if (pr.option != null) {
							defaultValueOptionsToCheck.remove(pr.option);
						}
					});
				defaultValueOptionsToCheck.stream()
					.filter(co -> co.getDefaultValue() != null)
					.forEach(co -> {
						Object value = co.getDefaultValue();
						value = convertOptionType(co, value);
						results.add(ParserResult.of(co, Collections.emptyList(), value, null));
					});
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

			private ConvertArgumentsHolder convertArguments(CommandOption option, List<String> arguments) {
				Object value = null;
				List<String> unmapped = new ArrayList<>();

				ResolvableType type = option.getType();
				int arityMin = option.getArityMin();
				int arityMax = option.getArityMax();

				if (arityMin < 0 && type != null) {
					if (type.isAssignableFrom(boolean.class)) {
						arityMin = 1;
						arityMax = 1;
					}
				}

				if (arityMax > 1 && arityMin > -1 && arityMax >= arityMin && (arguments.size() < arityMin || arguments.size() > arityMax)) {
					String ln = option.getLongNames() != null
							? Stream.of(option.getLongNames()).collect(Collectors.joining(","))
							: "";
					String sn = option.getShortNames() != null ? Stream.of(option.getShortNames())
							.map(n -> Character.toString(n)).collect(Collectors.joining(",")) : "";
					if (arguments.size() < arityMin) {
						String msg = String.format("Not enough arguments, longnames='%s', shortnames='%s'", ln, sn);
						return new ConvertArgumentsHolder(value, unmapped, new NotEnoughArgumentsOptionException(msg, option));
					}
					if (arguments.size() > arityMax) {
						String msg = String.format("Too many arguments, longnames='%s', shortnames='%s'", ln, sn);
						return new ConvertArgumentsHolder(value, unmapped, new TooManyArgumentsOptionException(msg, option));
					}
				}

				if (type != null && type.isAssignableFrom(boolean.class)) {
					if (arguments.size() == 0) {
						value = true;
					}
					else {
						value = Boolean.parseBoolean(arguments.get(0));
					}
				}
				else if (type != null && type.isArray()) {
					value = arguments.stream().collect(Collectors.toList()).toArray();
				}
				// if it looks like type is a collection just get as list
				// as conversion will happen later. we just need to know
				// if user has Set, List, Collection, etc without worrying
				// about generics.
				else if (type != null && type.asCollection() != ResolvableType.NONE) {
					value = arguments.stream().collect(Collectors.toList());
				}
				else {
					if (!arguments.isEmpty()) {
						if (arguments.size() == 1) {
							value = arguments.get(0);
						}
						else {
							if (arityMax > 0) {
								int limit = Math.min(arguments.size(), arityMax);
								value = arguments.stream().limit(limit).collect(Collectors.toList());
								unmapped.addAll(arguments.subList(limit, arguments.size()));
							}
							else {
								value = arguments.get(0);
								unmapped.addAll(arguments.subList(1, arguments.size()));
							}
						}
					}
				}

				return new ConvertArgumentsHolder(value, unmapped);
			}

			private class ConvertArgumentsHolder {
				Object value;
				final List<String> unmapped = new ArrayList<>();
				CommandParserException error;

				ConvertArgumentsHolder(Object value, List<String> unmapped) {
					this(value, unmapped, null);
				}

				ConvertArgumentsHolder(Object value, List<String> unmapped, CommandParserException error) {
					this.value = value;
					if (unmapped != null) {
						this.unmapped.addAll(unmapped);
					}
					this.error = error;
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
				return Utils.split(args, t -> t.startsWith("-"));
			}
		}
	}

	public static class CommandParserException extends RuntimeException {

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

	public static class OptionException extends CommandParserException {

		private CommandOption option;

		public OptionException(String message, CommandOption option) {
			super(message);
			this.option = option;
		}

		public CommandOption getOption() {
			return option;
		}
	}

	public static class TooManyArgumentsOptionException extends OptionException {

		public TooManyArgumentsOptionException(String message, CommandOption option) {
			super(message, option);
		}
	}

	public static class NotEnoughArgumentsOptionException extends OptionException {

		public NotEnoughArgumentsOptionException(String message, CommandOption option) {
			super(message, option);
		}
	}

	public static class MissingOptionException extends CommandParserException {

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

	public static class UnrecognisedOptionException extends CommandParserException {

		private String option;

		public UnrecognisedOptionException(String message, String option) {
			super(message);
			this.option = option;
		}

		public static UnrecognisedOptionException of(String message, String option) {
			return new UnrecognisedOptionException(message, option);
		}

		public String getOption() {
			return option;
		}
	}
}
