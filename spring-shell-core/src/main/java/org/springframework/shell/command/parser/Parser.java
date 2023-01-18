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
import java.util.List;

import org.springframework.shell.command.CommandRegistration;
import org.springframework.shell.command.parser.Ast.AstResult;
import org.springframework.shell.command.parser.CommandModel.CommandInfo;

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

			// 3. visit nodes
			//    whoever uses this parser can then do further
			//    things with final parsing results
			NodeVisitor visitor = new DefaultNodeVisitor(commandModel);
			return visitor.visit(commandNode);
		}
	}

	/**
	 * Default implementation of a {@link NodeVisitor}.
	 */
	static class DefaultNodeVisitor extends AbstractNodeVisitor {

		private final CommandModel commandModel;
		private List<String> resolvedCommmand = new ArrayList<>();

		DefaultNodeVisitor(CommandModel commandModel) {
			this.commandModel = commandModel;
		}

		@Override
		protected ParseResult buildResult() {
			CommandInfo info = commandModel.resolve(resolvedCommmand);
			CommandRegistration registration = info.registration;
			return new ParseResult(registration);
		}

		@Override
		protected void onRootCommandNode(CommandNode node) {
			resolvedCommmand.add(node.getCommand());
		}

		@Override
		protected void onCommandNode(CommandNode node) {
			resolvedCommmand.add(node.getCommand());
		}

		@Override
		protected void onOptionNode(OptionNode node) {
		}

		@Override
		protected void onCommandArgumentNode(CommandArgumentNode node) {
		}

		@Override
		protected void onOptionArgumentNode(OptionArgumentNode node) {
		}

	}
}
