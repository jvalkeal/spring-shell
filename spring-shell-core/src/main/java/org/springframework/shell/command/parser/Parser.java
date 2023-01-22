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
package org.springframework.shell.command.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.shell.command.CommandOption;
import org.springframework.shell.command.CommandRegistration;
import org.springframework.shell.command.parser.Ast.AstResult;
import org.springframework.shell.command.parser.CommandModel.CommandInfo;
import org.springframework.shell.command.parser.Lexer.LexerResult;
import org.springframework.shell.command.parser.Parser.ParseResult.ArgumentResult;
import org.springframework.shell.command.parser.Parser.ParseResult.OptionResult;

/**
 * Interface to parse command line arguments.
 *
 * @author Janne Valkealahti
 */
public interface Parser {

	/**
	 * Parse given arguments into a {@link ParseResult}.
	 *
	 * @param arguments the command line arguments
	 * @return a parsed results
	 */
	ParseResult parse(List<String> arguments);


	/**
	 * Results from a {@link Parser} containing needed information like resolved
	 * {@link CommandRegistration}, list of {@link CommandOption} instances, errors
	 * and directive.
	 *
	 * @param commandRegistration command registration
	 * @param optionResults option results
	 * @param directiveResults directive result
	 * @param messageResults message results
	 */
	public record ParseResult(CommandRegistration commandRegistration, List<OptionResult> optionResults,
			List<DirectiveResult> directiveResults, List<MessageResult> messageResults, List<ArgumentResult> argumentResults) {

		public record OptionResult(CommandOption option, Object value) {

			public static OptionResult of(CommandOption option, Object value) {
				return new OptionResult(option, value);
			}
		}

		public record ArgumentResult(String value, int position) {

			public static ArgumentResult of(String value, int position) {
				return new ArgumentResult(value, position);
			}
		}
	}

	/**
	 * Default implementation of a {@link Parser}. Uses {@link Lexer} and
	 * {@link Ast}.
	 */
	public class DefaultParser implements Parser {

		private final ParserConfiguration configuration;
		private final CommandModel commandModel;
		private final Lexer lexer;
		private final Ast ast;
		private ConversionService conversionService;

		public DefaultParser(CommandModel commandModel, Lexer lexer, Ast ast) {
			this(commandModel, lexer, ast, new ParserConfiguration());
		}

		public DefaultParser(CommandModel commandModel, Lexer lexer, Ast ast, ParserConfiguration configuration) {
			this(commandModel, lexer, ast, configuration, null);
		}

		public DefaultParser(CommandModel commandModel, Lexer lexer, Ast ast, ParserConfiguration configuration,
				ConversionService conversionService) {
			this.commandModel = commandModel;
			this.lexer = lexer;
			this.ast = ast;
			this.configuration = configuration;
			this.conversionService = conversionService != null ? conversionService : new DefaultConversionService();
		}

		@Override
		public ParseResult parse(List<String> arguments) {
			// 1. tokenize arguments
			LexerResult lexerResult = lexer.tokenize(arguments);
			List<Token> tokens = lexerResult.tokens();

			// 2. generate syntax tree results from tokens
			//    result from it is then feed into node visitor
			AstResult astResult = ast.generate(tokens);

			// 3. visit nodes
			//    whoever uses this parser can then do further
			//    things with final parsing results
			// XXX expose setting conversion service
			NodeVisitor visitor = new DefaultNodeVisitor(commandModel, conversionService);
			ParseResult parseResult = visitor.visit(astResult.nonterminalNodes(), astResult.terminalNodes());
			parseResult.messageResults().addAll(lexerResult.messageResults());
			return parseResult;
		}
	}

	/**
	 * Default implementation of a {@link NodeVisitor}.
	 */
	class DefaultNodeVisitor extends AbstractNodeVisitor {

		private final CommandModel commandModel;
		private final ConversionService conversionService;
		private List<String> resolvedCommmand = new ArrayList<>();
		private List<OptionResult> options = new ArrayList<>();
		private CommandOption currentOption;
		private List<String> currentOptionArgument = new ArrayList<>();
		private List<DirectiveResult> directiveResults = new ArrayList<>();
		private List<OptionNode> invalidOptionNodes = new ArrayList<>();
		private final List<MessageResult> errorResults = new ArrayList<>();
		private List<ArgumentResult> argumentResults = new ArrayList<>();

		DefaultNodeVisitor(CommandModel commandModel, ConversionService conversionService) {
			this.commandModel = commandModel;
			this.conversionService = conversionService;
		}

		@Override
		protected ParseResult buildResult() {
			CommandInfo info = commandModel.resolve(resolvedCommmand);
			CommandRegistration registration = info != null ? info.registration : null;

			List<MessageResult> errorResults = new ArrayList<>();
			if (registration != null) {
				errorResults.addAll(validate(registration));
			}

			return new ParseResult(registration, options, directiveResults, errorResults, argumentResults);
		}

		List<MessageResult> validate(CommandRegistration registration) {

			// option missing
			// XXX getOptions() build new CommandOption instances
			//     can't live with this hack
			Map<String, CommandOption> requiredOptions = registration.getOptions().stream()
				.filter(o -> o.isRequired())
				.collect(Collectors.toMap(o -> {
					String part1 = Stream.of(o.getLongNames()).sorted().collect(Collectors.joining());
					String part2 = Stream.of(o.getShortNames()).map(s -> s.toString()).sorted().collect(Collectors.joining());
					return part1 + part2;
				}, o -> o))
			;

			options.stream().map(or -> or.option()).forEach(o -> {
				String part1 = Stream.of(o.getLongNames()).sorted().collect(Collectors.joining());
				String part2 = Stream.of(o.getShortNames()).map(s -> s.toString()).sorted().collect(Collectors.joining());
				String key = part1 + part2;
				requiredOptions.remove(key);
			});

			requiredOptions.values().forEach(o -> {
				String ln = o.getLongNames() != null ? Stream.of(o.getLongNames()).collect(Collectors.joining(",")) : "";
				String sn = o.getShortNames() != null ? Stream.of(o.getShortNames()).map(n -> Character.toString(n))
						.collect(Collectors.joining(",")) : "";
				errorResults.add(MessageResult.of(ParserMessage.MANDATORY_OPTION_MISSING, 0, ln, sn));
			});

			// invalid option
			invalidOptionNodes.forEach(on -> {
				errorResults.add(MessageResult.of(ParserMessage.UNRECOGNISED_OPTION, 0, on.getName()));
			});

			return errorResults;
		}

		@Override
		protected void onEnterDirectiveNode(DirectiveNode node) {
			directiveResults.add(DirectiveResult.of(node.getName(), node.getValue()));
		}

		@Override
		protected void onExitDirectiveNode(DirectiveNode node) {
		}

		@Override
		protected void onEnterRootCommandNode(CommandNode node) {
			resolvedCommmand.add(node.getCommand());
		}

		@Override
		protected void onExitRootCommandNode(CommandNode node) {
		}

		@Override
		protected void onEnterCommandNode(CommandNode node) {
			resolvedCommmand.add(node.getCommand());
		}

		@Override
		protected void onExitCommandNode(CommandNode node) {
		}

		@Override
		protected void onEnterOptionNode(OptionNode node) {
			currentOptionArgument.clear();
			CommandInfo info = commandModel.resolve(resolvedCommmand);
			String name = node.getName();
			info.registration.getOptions().forEach(option -> {
				Set<String> longNames = Arrays.asList(option.getLongNames()).stream()
					.map(n -> "--" + n)
					.collect(Collectors.toSet());
				boolean match = longNames.contains(name);
				if (!match) {
					Set<String> shortNames = Arrays.asList(option.getShortNames()).stream()
						.map(n -> "-" + Character.toString(n))
						.collect(Collectors.toSet());
					match = shortNames.contains(name);
				}
				if (match) {
						currentOption = option;
					}
				});
		}

		@Override
		protected void onExitOptionNode(OptionNode node) {
			if (currentOption != null) {
				// Object value = currentOptionArgument;
				Object value = null;
				if (currentOptionArgument.size() == 1) {
					value = currentOptionArgument.get(0);
				}
				else if (currentOptionArgument.size() > 1) {
					value = new ArrayList<>(currentOptionArgument);
				}
				try {
					// value = convertOptionType(currentOption, currentOptionArgument);
					value = convertOptionType(currentOption, value);
				} catch (Exception e) {
					errorResults.add(MessageResult.of(ParserMessage.ILLEGAL_OPTION_VALUE, 0, value, e.getMessage()));
				}
				// Object value = convertOptionType(currentOption, currentOptionArgument);
				// options.add(new OptionResult(currentOption, currentOptionArgument));
				options.add(new OptionResult(currentOption, value));
			}
			else {
				invalidOptionNodes.add(node);
			}
			currentOption = null;
		}

		@Override
		protected void onEnterCommandArgumentNode(CommandArgumentNode node) {
			argumentResults.add(ArgumentResult.of(node.getToken().getValue(), node.getToken().getPosition()));
		}

		@Override
		protected void onExitCommandArgumentNode(CommandArgumentNode node) {
		}

		@Override
		protected void onEnterOptionArgumentNode(OptionArgumentNode node) {
			// currentOptionArgument = node.getValue();
			currentOptionArgument.add(node.getValue());
		}

		@Override
		protected void onExitOptionArgumentNode(OptionArgumentNode node) {
		}

		private Object convertOptionType(CommandOption option, Object value) {
			if (conversionService != null && option.getType() != null && value != null) {
				if (conversionService.canConvert(value.getClass(), option.getType().getRawClass())) {
					value = conversionService.convert(value, option.getType().getRawClass());
				}
			}
			return value;
		}
	}
}
