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

import java.util.Arrays;
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
	void oneRootNoOptionsNoChilds() {
		register(ROOT1);
		List<Token> tokens = tokenize("root1");

		assertThat(tokens).hasSize(1);

		assertThat(tokens.get(0)).satisfies(token -> {
			assertThat(token).isNotNull();
			assertThat(token.getType()).isEqualTo(TokenType.COMMAND);
			assertThat(token.getValue()).isEqualTo("root1");
			assertThat(token.getPosition()).isEqualTo(0);
		});
	}

	@Test
	void twoRootsOneWithChild() {
		register(ROOT1);
		register(ROOT2_SUB1);
		List<Token> tokens = tokenize("root1");

		assertThat(tokens).hasSize(1);

		assertThat(tokens.get(0)).satisfies(token -> {
			assertThat(token).isNotNull();
			assertThat(token.getType()).isEqualTo(TokenType.COMMAND);
			assertThat(token.getValue()).isEqualTo("root1");
			assertThat(token.getPosition()).isEqualTo(0);
		});
	}

	@Test
	void optionFromRoot() {
		register(ROOT3);
		List<Token> tokens = tokenize("root3", "--arg1");

		assertThat(tokens).hasSize(2);

		assertThat(tokens.get(0)).satisfies(token -> {
			assertThat(token).isNotNull();
			assertThat(token.getType()).isEqualTo(TokenType.COMMAND);
			assertThat(token.getValue()).isEqualTo("root3");
			assertThat(token.getPosition()).isEqualTo(0);
		});

		assertThat(tokens.get(1)).satisfies(token -> {
			assertThat(token).isNotNull();
			assertThat(token.getType()).isEqualTo(TokenType.OPTION);
			assertThat(token.getValue()).isEqualTo("--arg1");
			assertThat(token.getPosition()).isEqualTo(1);
		});
	}

	@Test
	void doubleDashAddsCommandArguments() {
		register(ROOT3);
		List<Token> tokens = tokenize("root3", "--", "arg1");

		assertThat(tokens).hasSize(3);

		assertThat(tokens).extracting(token -> token.getType()).containsExactly(TokenType.COMMAND, TokenType.DOUBLEDASH,
				TokenType.ARGUMENT);

	}

	@Test
	void directiveWithCommand() {
		register(ROOT1);
		ParserConfiguration configuration = new ParserConfiguration().setEnableDirectives(true);
		List<Token> tokens = tokenize(lexer(configuration), "[fake]", "root1");

		assertThat(tokens).hasSize(2);

		assertThat(tokens.get(0)).satisfies(token -> {
			assertThat(token).isNotNull();
			assertThat(token.getType()).isEqualTo(TokenType.DIRECTIVE);
			assertThat(token.getValue()).isEqualTo("[fake]");
			assertThat(token.getPosition()).isEqualTo(0);
		});

		assertThat(tokens.get(1)).satisfies(token -> {
			assertThat(token).isNotNull();
			assertThat(token.getType()).isEqualTo(TokenType.COMMAND);
			assertThat(token.getValue()).isEqualTo("root1");
			assertThat(token.getPosition()).isEqualTo(1);
		});
	}
}
