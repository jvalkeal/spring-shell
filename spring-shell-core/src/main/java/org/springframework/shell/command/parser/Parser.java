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

import org.springframework.shell.command.CommandOption;
import org.springframework.shell.command.CommandRegistration;
import org.springframework.shell.command.parser.Ast.AstResult;
import org.springframework.shell.command.parser.CommandModel.CommandInfo;
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
			List<Token> tokens = lexer.tokenize(arguments);

			// 2. generate syntax tree results from tokens
			//    should then get top node which we can later visit
			AstResult astResult = ast.generate(tokens);
			CommandNode commandNode = astResult.getCommandNode();
			DirectiveNode directiveNode = astResult.getDirectiveNode();

			// 3. visit nodes
			//    whoever uses this parser can then do further
			//    things with final parsing results
			NodeVisitor visitor = new DefaultNodeVisitor(commandModel);
			return visitor.visit(commandNode, directiveNode);
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

		DefaultNodeVisitor(CommandModel commandModel) {
			this.commandModel = commandModel;
		}

		@Override
		protected ParseResult buildResult() {
			CommandInfo info = commandModel.resolve(resolvedCommmand);
			CommandRegistration registration = info.registration;
			return new ParseResult(registration, options, directive);
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
			options.add(new OptionResult(currentOption, currentOptionArgument));
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
