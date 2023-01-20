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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for default implementation in a {@link Lexer}.
 *
 * @author Janne Valkealahti
 */
class LexerTests extends AbstractParsingTests {

	@Test
	void rootCommandNoOptions() {
		register(ROOT1);
		List<Token> tokens = tokenize("root1");

		assertThat(tokens).satisfiesExactly(
				token -> {
					ParserAssertions.assertThat(token)
						.isType(TokenType.COMMAND)
						.hasPosition(0)
						.hasValue("root1");
				});
	}

	@Test
	void rootCommandWithChildGetsRootForRoot() {
		register(ROOT1);
		register(ROOT2_SUB1);
		List<Token> tokens = tokenize("root1");

		assertThat(tokens).satisfiesExactly(
			token -> {
				ParserAssertions.assertThat(token)
					.isType(TokenType.COMMAND)
					.hasPosition(0)
					.hasValue("root1");
			});
	}

	@Test
	void definedOptionShouldBeOptionAfterCommand() {
		register(ROOT3);
		List<Token> tokens = tokenize("root3", "--arg1");

		assertThat(tokens).satisfiesExactly(
				token -> {
					ParserAssertions.assertThat(token)
						.isType(TokenType.COMMAND)
						.hasPosition(0)
						.hasValue("root3");
				},
				token -> {
					ParserAssertions.assertThat(token)
						.isType(TokenType.OPTION)
						.hasPosition(1)
						.hasValue("--arg1");
				});
	}

	@Test
	void notDefinedOptionShouldBeOptionAfterCommand() {
		register(ROOT3);
		List<Token> tokens = tokenize("root3", "--arg2");

		assertThat(tokens).satisfiesExactly(
				token -> {
					ParserAssertions.assertThat(token)
						.isType(TokenType.COMMAND)
						.hasPosition(0)
						.hasValue("root3");
				},
				token -> {
					ParserAssertions.assertThat(token)
						.isType(TokenType.OPTION)
						.hasPosition(1)
						.hasValue("--arg2");
				});
	}

	@Test
	void notDefinedOptionShouldBeOptionAfterDefinedOption() {
		register(ROOT3);
		List<Token> tokens = tokenize("root3", "--arg1", "--arg2");

		assertThat(tokens).satisfiesExactly(
				token -> {
					ParserAssertions.assertThat(token)
						.isType(TokenType.COMMAND)
						.hasPosition(0)
						.hasValue("root3");
				},
				token -> {
					ParserAssertions.assertThat(token)
						.isType(TokenType.OPTION)
						.hasPosition(1)
						.hasValue("--arg1");
				},
				token -> {
					ParserAssertions.assertThat(token)
						.isType(TokenType.OPTION)
						.hasPosition(2)
						.hasValue("--arg2");
				});
	}

	@Test
	void optionsWithoutValuesFromRoot() {
		register(ROOT5);
		List<Token> tokens = tokenize("root5", "--arg1", "--arg2");

		assertThat(tokens).satisfiesExactly(
				token -> {
					ParserAssertions.assertThat(token)
						.isType(TokenType.COMMAND)
						.hasPosition(0)
						.hasValue("root5");
				},
				token -> {
					ParserAssertions.assertThat(token)
						.isType(TokenType.OPTION)
						.hasPosition(1)
						.hasValue("--arg1");
				},
				token -> {
					ParserAssertions.assertThat(token)
						.isType(TokenType.OPTION)
						.hasPosition(2)
						.hasValue("--arg2");
				});
	}

	@Test
	void optionsWithValuesFromRoot() {
		register(ROOT5);
		List<Token> tokens = tokenize("root5", "--arg1", "value1", "--arg2", "value2");

		assertThat(tokens).satisfiesExactly(
				token -> {
					ParserAssertions.assertThat(token)
						.isType(TokenType.COMMAND)
						.hasPosition(0)
						.hasValue("root5");
				},
				token -> {
					ParserAssertions.assertThat(token)
						.isType(TokenType.OPTION)
						.hasPosition(1)
						.hasValue("--arg1");
				},
				token -> {
					ParserAssertions.assertThat(token)
						.isType(TokenType.ARGUMENT)
						.hasPosition(2)
						.hasValue("value1");
				},
				token -> {
					ParserAssertions.assertThat(token)
						.isType(TokenType.OPTION)
						.hasPosition(3)
						.hasValue("--arg2");
				},
				token -> {
					ParserAssertions.assertThat(token)
						.isType(TokenType.ARGUMENT)
						.hasPosition(4)
						.hasValue("value2");
				});
	}

	@Test
	void optionValueFromRoot() {
		register(ROOT3);
		List<Token> tokens = tokenize("root3", "--arg1", "value1");

		assertThat(tokens).extracting(Token::getType).containsExactly(TokenType.COMMAND, TokenType.OPTION,
				TokenType.ARGUMENT);
	}

	@Test
	void doubleDashAddsCommandArguments() {
		register(ROOT3);
		List<Token> tokens = tokenize("root3", "--", "arg1");

		assertThat(tokens).extracting(Token::getType).containsExactly(TokenType.COMMAND, TokenType.DOUBLEDASH,
				TokenType.ARGUMENT);
	}

	@Test
	void commandArgsWithoutOption() {
		register(ROOT3);
		List<Token> tokens = tokenize("root3", "value1", "value2");

		assertThat(tokens).extracting(Token::getType).containsExactly(TokenType.COMMAND, TokenType.ARGUMENT,
				TokenType.ARGUMENT);
	}

	@Test
	void directiveWithCommand() {
		register(ROOT1);
		ParserConfiguration configuration = new ParserConfiguration().setEnableDirectives(true);
		List<Token> tokens = tokenize(lexer(configuration), "[fake]", "root1");

		assertThat(tokens).satisfiesExactly(
				token -> {
					ParserAssertions.assertThat(token)
						.isType(TokenType.DIRECTIVE)
						.hasPosition(0)
						.hasValue("[fake]");
				},
				token -> {
					ParserAssertions.assertThat(token)
						.isType(TokenType.COMMAND)
						.hasPosition(1)
						.hasValue("root1");
				});
	}

	@Test
	void ignoreDirectiveIfDisabled() {
		register(ROOT1);
		List<Token> tokens = tokenize("[fake]", "root1");

		assertThat(tokens).extracting(Token::getType).containsExactly(TokenType.COMMAND);
	}

}
