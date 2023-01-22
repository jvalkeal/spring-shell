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

import org.junit.jupiter.api.Test;

import org.springframework.shell.command.parser.Parser.ParseResult;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for default implementations in/with a {@link Parser}.
 *
 * @author Janne Valkealahti
 */
class ParserTests extends AbstractParsingTests {

	@Test
	void shouldFindRegistration() {
		register(ROOT1);
		ParseResult result = parse("root1");
		assertThat(result).isNotNull();
		assertThat(result.commandRegistration()).isNotNull();
		assertThat(result.messageResults()).isEmpty();
	}

	@Test
	void shouldFindRegistrationCaseInsensitive() {
		register(ROOT1_UP);
		ParserConfiguration configuration = new ParserConfiguration()
				.setCommandsCaseSensitive(false)
				.setOptionsCaseSensitive(false);
		ParseResult result = parse(configuration, "root1");
		assertThat(result).isNotNull();
		assertThat(result.commandRegistration()).isNotNull();
		assertThat(result.messageResults()).isEmpty();
	}

	@Test
	void shouldFindOption() {
		register(ROOT3);
		ParseResult result = parse("root3", "--arg1");
		assertThat(result).isNotNull();
		assertThat(result.commandRegistration()).isNotNull();
		assertThat(result.optionResults()).isNotEmpty();
		assertThat(result.messageResults()).isEmpty();
	}

	@Test
	void shouldFindOptionArgument() {
		register(ROOT3);
		ParseResult result = parse("root3", "--arg1", "value1");
		assertThat(result).isNotNull();
		assertThat(result.commandRegistration()).isNotNull();
		assertThat(result.optionResults()).isNotEmpty();
		assertThat(result.optionResults().get(0).value()).isEqualTo("value1");
		assertThat(result.messageResults()).isEmpty();
	}

	@Test
	void directiveNoValueWithCommand() {
		register(ROOT3);
		ParserConfiguration configuration = new ParserConfiguration().setEnableDirectives(true);

		ParseResult result = parse(lexer(configuration), "[fake]", "root3");
		assertThat(result.directiveResults()).satisfiesExactly(d -> {
			assertThat(d.name()).isEqualTo("fake");
			assertThat(d.value()).isNull();
		});
		assertThat(result.messageResults()).isEmpty();
	}

	@Test
	void directiveWithValueWithCommand() {
		register(ROOT3);
		ParserConfiguration configuration = new ParserConfiguration().setEnableDirectives(true);

		ParseResult result = parse(lexer(configuration), "[fake:value]", "root3");
		assertThat(result.directiveResults()).satisfiesExactly(d -> {
			assertThat(d.name()).isEqualTo("fake");
			assertThat(d.value()).isEqualTo("value");
		});
		assertThat(result.messageResults()).isEmpty();
	}

	@Test
	void multipleDirectivesWithCommand() {
		register(ROOT3);
		ParserConfiguration configuration = new ParserConfiguration().setEnableDirectives(true);

		ParseResult result = parse(lexer(configuration), "[foo][bar:value]", "root3");
		assertThat(result.directiveResults()).satisfiesExactly(
			d -> {
				assertThat(d.name()).isEqualTo("foo");
				assertThat(d.value()).isNull();
			},
			d -> {
				assertThat(d.name()).isEqualTo("bar");
				assertThat(d.value()).isEqualTo("value");
			});
		assertThat(result.messageResults()).isEmpty();
	}

	@Test
	void shouldHaveErrorResult() {
		register(ROOT4);
		ParseResult result = parse("root4");
		assertThat(result).isNotNull();
		assertThat(result.messageResults()).satisfiesExactly(message -> {
			ParserAssertions.assertThat(message.parserMessage()).hasCode(2000).hasType(ParserMessage.Type.ERROR);
		});
		// "100E:(pos 0): Missing option, longnames='arg1', shortnames=''"
		assertThat(result.messageResults().get(0).getMessage()).contains("Missing option", "arg1");
	}

	@Test
	void shouldHaveErrorResult2() {
		register(ROOT4);
		// ParseResult result = parse("root4", "--arg1", "value1", "--arg2", "value2");
		ParseResult result = parse("root4", "--arg1", "--arg2");
		assertThat(result).isNotNull();
		assertThat(result.messageResults()).satisfiesExactly(message -> {
			ParserAssertions.assertThat(message.parserMessage()).hasCode(2001).hasType(ParserMessage.Type.ERROR);
		});
		// "101E:(pos 0): Unrecognised option '--arg2'"
		// assertThat(result.messageResults().get(0).getMessage()).contains("xxx");
	}

	@Test
	void lexerMessageShouldGetPropagated() {
		ParseResult parse = parse("--");
		assertThat(parse.messageResults()).extracting(ms -> ms.parserMessage().getCode()).containsExactly(1000, 1000);
	}

	@Test
	void optionValueShouldBeInteger() {
		register(ROOT6_OPTION_INT);
		ParseResult result = parse("root6", "--arg1", "1");
		assertThat(result).isNotNull();
		assertThat(result.commandRegistration()).isNotNull();
		assertThat(result.optionResults()).isNotEmpty();
		assertThat(result.optionResults().get(0).value()).isEqualTo(1);
		assertThat(result.messageResults()).isEmpty();
	}

	@Test
	void optionValueShouldBeIntegerArray() {
		register(ROOT6_OPTION_INTARRAY);
		ParseResult result = parse("root6", "--arg1", "1", "2");
		assertThat(result).isNotNull();
		assertThat(result.commandRegistration()).isNotNull();
		assertThat(result.optionResults()).isNotEmpty();
		assertThat(result.optionResults().get(0).value()).isEqualTo(new int[] { 1, 2 });
		assertThat(result.messageResults()).isEmpty();
	}

	@Test
	void optionValueFailsFromStringToInteger() {
		register(ROOT6_OPTION_INT);
		ParseResult result = parse("root6", "--arg1", "x");
		assertThat(result).isNotNull();
		assertThat(result.commandRegistration()).isNotNull();
		assertThat(result.optionResults()).isNotEmpty();
		assertThat(result.optionResults().get(0).value()).isEqualTo("x");
		assertThat(result.messageResults()).isNotEmpty();
		assertThat(result.messageResults()).satisfiesExactly(ms -> {
			// "2002E:(pos 0): Illegal option value 'x', reason 'Failed to convert from type [java.lang.String] to type [int] for value 'x''"
			assertThat(ms.getMessage()).contains("Failed to convert");
		});
	}

}
