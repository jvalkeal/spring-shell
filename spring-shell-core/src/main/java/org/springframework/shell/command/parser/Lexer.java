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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.shell.command.parser.CommandModel.CommandInfo;

/**
 * Interface to tokenize arguments into tokens. Generic language parser usually
 * contains lexing and parsing where this {@code Lexer} represents the former
 * lexing side.
 *
 * Lexing takes a first step to analyse basic construct of elements out from
 * given arguments. We get rough idea what each argument represents but don't
 * look deeper if any of it is correct which happens later when tokens go
 * through parsing operation.
 *
 * @author Janne Valkealahti
 */
public interface Lexer {

	/**
	 * Tokenize given command line arguments into a list of tokens.
	 *
	 * @param arguments the command line arguments
	 * @return lexer result having tokens and operation messages
	 */
	LexerResult tokenize(List<String> arguments);

	/**
	 * Representing result from {@link Lexer} tokenisation.
	 *
	 * @param tokens list of tokens in this result
	 * @param messageResults list of error results in this result
	 */
	public record LexerResult(List<Token> tokens, List<MessageResult> messageResults) {
	}

	/**
	 * Default implementation of a {@link Lexer}.
	 */
	public class DefaultLexer implements Lexer {

		private final static Logger log = LoggerFactory.getLogger(DefaultLexer.class);
		private final CommandModel commandModel;
		private final ParserConfiguration configuration;

		public DefaultLexer(CommandModel commandModel, ParserConfiguration configuration) {
			this.commandModel = commandModel;
			this.configuration = configuration;
		}

		@Override
		public LexerResult tokenize(List<String> arguments) {
			log.debug("Tokenizing arguments {}", arguments);
			List<MessageResult> errorResults = new ArrayList<>();

			preValidate(errorResults, arguments);
			boolean foundDoubleDash = false;
			boolean foundEndOfDirectives = !configuration.isEnableDirectives();

			List<Token> tokenList = new ArrayList<Token>(arguments.size());

			// starting from root level
			Map<String, Token> validTokens = commandModel.getValidRootTokens();

			CommandInfo currentCommand = null;

			int i = -1;
			for (String argument : arguments) {
				i++;

				// We've found bash style "--" meaning further option processing is
				// stopped and remaining arguments are simply command arguments
				if (foundDoubleDash) {
					tokenList.add(Token.of(argument, TokenType.ARGUMENT, i));
					continue;
				}
				if (!foundDoubleDash && "--".equals(argument)) {
						tokenList.add(Token.of(argument, TokenType.DOUBLEDASH, i));
						foundDoubleDash = true;
						continue;
				}

				// take directive if its defined
				// we can always be done with directives as those become before rest
				if (!foundEndOfDirectives) {
					if (argument.length() > 2 && argument.charAt(0) == '[' && argument.charAt(1) != ']'
							&& argument.charAt(1) != ':' && argument.charAt(argument.length() - 1) == ']') {
						tokenList.add(Token.of(argument, TokenType.DIRECTIVE, i));
						continue;
					}
					foundEndOfDirectives = true;
				}

				if (validTokens.containsKey(argument)) {
					Token token = validTokens.get(argument);
					switch (token.getType()) {
						case COMMAND:
							currentCommand = commandModel.getRootCommands().get(argument);
							tokenList.add(Token.of(argument, TokenType.COMMAND, i));
							validTokens = currentCommand.getValidTokens();
							break;
						case OPTION:
							tokenList.add(Token.of(argument, TokenType.OPTION, i));
							break;
						default:
							break;
					}
				}
				else if (isLastTokenOfType(tokenList, TokenType.OPTION)) {
					if (argument.startsWith("-")) {
						tokenList.add(Token.of(argument, TokenType.OPTION, i));
					}
					else {
						tokenList.add(Token.of(argument, TokenType.ARGUMENT, i));
					}
				}
				else if (isLastTokenOfType(tokenList, TokenType.COMMAND)) {
					if (argument.startsWith("-")) {
						tokenList.add(Token.of(argument, TokenType.OPTION, i));
					}
					else {
						tokenList.add(Token.of(argument, TokenType.ARGUMENT, i));
					}
				}
				else if (isLastTokenOfType(tokenList, TokenType.ARGUMENT)) {
					tokenList.add(Token.of(argument, TokenType.ARGUMENT, i));
				}

			}

			log.debug("Generated token list {}", tokenList);
			return new LexerResult(tokenList, errorResults);
		}

		private void preValidate(List<MessageResult> errorResults, List<String> arguments) {
			if (arguments.size() > 0) {
				String arg = arguments.get(0);
				if ("--".equals(arg)) {
					errorResults.add(MessageResult.of(ParserMessage.ILLEGAL_CONTENT_BEFORE_COMMANDS, 0, arg));
				}
			}
		}

		private static boolean isLastTokenOfType(List<Token> tokenList, TokenType type) {
			if (tokenList.size() > 0) {
				if (tokenList.get(tokenList.size() - 1).getType() == type) {
					return true;
				}
			}
			return false;
		}
	}
}
