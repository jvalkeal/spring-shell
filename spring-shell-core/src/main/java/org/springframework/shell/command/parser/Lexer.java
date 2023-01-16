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
import java.util.Map;

import org.springframework.shell.command.parser.CommandModel.CommandInfo;

/**
 * Interface to tokenize arguments into tokens. Generic language parser usually
 * contains lexing and parsing where this {@code Lexer} represents the former
 * lexing side.
 *
 * @author Janne Valkealahti
 */
public interface Lexer {

	/**
	 * Tokenize given command line arguments into a list of tokens.
	 *
	 * @param arguments the command line arguments
	 * @return list of tokens
	 */
	List<Token> tokenize(List<String> arguments);

	/**
	 * Default implementation of a {@link Lexer}.
	 */
	public static class DefaultLexer implements Lexer {

		private final CommandModel commandModel;
		private final ParserConfiguration configuration;

		public DefaultLexer(CommandModel commandModel, ParserConfiguration configuration) {
			this.commandModel = commandModel;
			this.configuration = configuration;
		}

		@Override
		public List<Token> tokenize(List<String> arguments) {

			var foundDoubleDash = false;
			var foundEndOfDirectives = !configuration.isEnableDirectives();

			List<Token> tokenList = new ArrayList<Token>(arguments.size());

			int i = -1;
			for (String argument : arguments) {
				i++;

				if (foundDoubleDash) {
					tokenList.add(Token.of(argument, TokenType.ARGUMENT, i));
					continue;
				}

				if (!foundDoubleDash && "--".equals(argument)) {
						tokenList.add(Token.of(argument, TokenType.DOUBLEDASH, i));
						foundDoubleDash = true;
						continue;
				}

				// finding a command marks no-more-directives as those
				// are always before commands.
				if (!foundEndOfDirectives) {
					if (argument.length() > 2 && argument.charAt(0) == '[' && argument.charAt(1) != ']'
							&& argument.charAt(1) != ':' && argument.charAt(argument.length() - 1) == ']') {
						tokenList.add(Token.of(argument, TokenType.DIRECTIVE, i));
						continue;
					}
				}

				// if (!configuration.RootCommand.HasAlias(arg))
				// {
				// 	foundEndOfDirectives = true;
				// }


				// Set<String> validTokens = commandModel.getValidRootTokens().keySet();
				Map<String, Token> validRootTokens = commandModel.getValidRootTokens();
				if (validRootTokens.containsKey(argument)) {
					Token token = validRootTokens.get(argument);
					switch (token.getType()) {
						case OPTION:
							// tokenList.Add(Option(arg, (Option)token.Symbol!));
							// tokenList.add(Token.of(argument, TokenType.OPTION, i));
							break;
						case COMMAND:
							break;
						default:
							break;
					}
				}

				CommandInfo info = commandModel.getRootCommand(argument);
				tokenList.addAll(commandModel.getValidRootTokens().values());
			}

			return tokenList;
		}

	}
}
