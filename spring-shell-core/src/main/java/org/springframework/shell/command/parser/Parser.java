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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.shell.command.CommandOption;
import org.springframework.shell.command.CommandRegistration;
import org.springframework.shell.command.parser.Ast.AstResult;
import org.springframework.shell.command.parser.CommandModel.CommandInfo;
import org.springframework.shell.command.parser.Lexer.LexerResult;
import org.springframework.shell.command.parser.ParseResult.OptionResult;

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
	 * Default implementation of a {@link Parser}. Uses {@link Lexer} and
	 * {@link Ast}.
	 */
	public static class DefaultParser implements Parser {

		private final ParserConfiguration configuration;
		private final CommandModel commandModel;
		private final Lexer lexer;
		private final Ast ast;

		DefaultParser(CommandModel commandModel, Lexer lexer, Ast ast) {
			this(commandModel, lexer, ast, new ParserConfiguration());
		}

		DefaultParser(CommandModel commandModel, Lexer lexer, Ast ast, ParserConfiguration configuration) {
			this.commandModel = commandModel;
			this.lexer = lexer;
			this.ast = ast;
			this.configuration = configuration;
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
			NodeVisitor visitor = new DefaultNodeVisitor(commandModel);
			ParseResult parseResult = visitor.visit(astResult.getNonterminalNodes(), astResult.getTerminalNodes());
			parseResult.getErrorResults().addAll(lexerResult.errorResults());
			return parseResult;
		}
	}

	/**
	 * Default implementation of a {@link NodeVisitor}.
	 */
	static class DefaultNodeVisitor extends AbstractNodeVisitor {

		private final CommandModel commandModel;
		private List<String> resolvedCommmand = new ArrayList<>();
		private CommandNode currentCommandNode;
		private List<OptionResult> options = new ArrayList<>();
		private CommandOption currentOption;
		private Object currentOptionArgument = null;
		private String directive = null;
		private List<OptionNode> invalidOptionNodes = new ArrayList<>();

		DefaultNodeVisitor(CommandModel commandModel) {
			this.commandModel = commandModel;
		}

		@Override
		protected ParseResult buildResult() {
			CommandInfo info = commandModel.resolve(resolvedCommmand);
			CommandRegistration registration = info != null ? info.registration : null;

			List<MessageResult> errorResults = new ArrayList<>();
			if (registration != null) {
				errorResults.addAll(validate(registration));
			}

			return new ParseResult(registration, options, directive, errorResults);
		}

		List<MessageResult> validate(CommandRegistration registration) {
			List<MessageResult> errorResults = new ArrayList<>();

			// option missing
			List<CommandOption> requiredOptions = registration.getOptions().stream()
				.filter(o -> o.isRequired())
				.collect(Collectors.toList());
			options.stream().map(or -> or.getOption()).forEach(o -> {
				// XXX getOptions() build new CommandOption instances
				// revisit changes did to CommandOption
				requiredOptions.remove(o);
			});
			requiredOptions.stream().forEach(o -> {
				String ln = o.getLongNames() != null ? Stream.of(o.getLongNames()).collect(Collectors.joining(",")) : "";
				String sn = o.getShortNames() != null ? Stream.of(o.getShortNames()).map(n -> Character.toString(n))
						.collect(Collectors.joining(",")) : "";
				errorResults.add(new MessageResult(ParserMessage.MANDATORY_OPTION_MISSING, 0, ln, sn));
			});

			// invalid option
			invalidOptionNodes.forEach(on -> {
				errorResults.add(new MessageResult(ParserMessage.UNRECOGNISED_OPTION, 0, on.getName()));
			});

			return errorResults;
		}

		@Override
		protected void onEnterDirectiveNode(DirectiveNode node) {
			directive = node.getName();
		}

		@Override
		protected void onExitDirectiveNode(DirectiveNode node) {
		}

		@Override
		protected void onEnterRootCommandNode(CommandNode node) {
			currentCommandNode = node;
			resolvedCommmand.add(node.getCommand());
		}

		@Override
		protected void onExitRootCommandNode(CommandNode node) {
		}

		@Override
		protected void onEnterCommandNode(CommandNode node) {
			currentCommandNode = node;
			resolvedCommmand.add(node.getCommand());
		}

		@Override
		protected void onExitCommandNode(CommandNode node) {
		}

		@Override
		protected void onEnterOptionNode(OptionNode node) {
			CommandInfo info = commandModel.resolve(resolvedCommmand);
			String name = node.getName();
			info.registration.getOptions().forEach(option -> {
				Set<String> longNames = Arrays.asList(option.getLongNames()).stream()
					.map(n -> "--" + n)
					.collect(Collectors.toSet());
				if (longNames.contains(name)) {
					currentOption = option;
				}
			});
		}

		@Override
		protected void onExitOptionNode(OptionNode node) {
			if (currentOption != null) {
				options.add(new OptionResult(currentOption, currentOptionArgument));
			}
			else {
				invalidOptionNodes.add(node);
			}
			currentOption = null;
		}

		@Override
		protected void onEnterCommandArgumentNode(CommandArgumentNode node) {
		}

		@Override
		protected void onExitCommandArgumentNode(CommandArgumentNode node) {
		}

		@Override
		protected void onEnterOptionArgumentNode(OptionArgumentNode node) {
			currentOptionArgument = node.getValue();
		}

		@Override
		protected void onExitOptionArgumentNode(OptionArgumentNode node) {
		}
	}
}
