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

import java.util.List;

import org.springframework.shell.command.parser.Ast.AstResult;

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

			// 2. generate syntax tree
			AstResult astResult = ast.generate(tokens);
			CommandNode commandNode = astResult.getCommandNode();

			// 3. visit nodes to get parsing result
			NodeVisitor visitor = new NodeVisitor();
			return visitor.visit(commandNode);
		}
	}
}
