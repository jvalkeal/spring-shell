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
		assertThat(result.getCommandRegistration()).isNotNull();
		assertThat(result.getErrorResults()).isEmpty();
	}

	@Test
	void shouldFindOption() {
		register(ROOT3);
		ParseResult result = parse("root3", "--arg1");
		assertThat(result).isNotNull();
		assertThat(result.getCommandRegistration()).isNotNull();
		assertThat(result.getOptionResults()).isNotEmpty();
		assertThat(result.getErrorResults()).isEmpty();
	}

	@Test
	void shouldFindOptionArgument() {
		register(ROOT3);
		ParseResult result = parse("root3", "--arg1", "value1");
		assertThat(result).isNotNull();
		assertThat(result.getCommandRegistration()).isNotNull();
		assertThat(result.getOptionResults()).isNotEmpty();
		assertThat(result.getOptionResults().get(0).getValue()).isEqualTo("value1");
		assertThat(result.getErrorResults()).isEmpty();
	}

	@Test
	void directiveWithCommand() {
		register(ROOT3);
		ParserConfiguration configuration = new ParserConfiguration().setEnableDirectives(true);
		ParseResult result = parse(lexer(configuration), "[fake]", "root3");
		assertThat(result.getDirective()).isEqualTo("fake");
		assertThat(result.getErrorResults()).isEmpty();
	}

	@Test
	void shouldHaveErrorResult() {
		register(ROOT4);
		ParseResult result = parse("root4");
		assertThat(result).isNotNull();
		assertThat(result.getErrorResults()).isNotEmpty();
		assertThat(result.getErrorResults().get(0).getParserMessage().getCode()).isEqualTo(2000);
		assertThat(result.getErrorResults().get(0).getParserMessage().getType()).isEqualTo(ParserMessage.Type.ERROR);
		// "100E:(pos 0): Missing option, longnames='arg1', shortnames=''"
		assertThat(result.getErrorResults().get(0).getMessage()).contains("Missing option", "arg1");
	}

	@Test
	void shouldHaveErrorResult2() {
		register(ROOT4);
		// ParseResult result = parse("root4", "--arg1", "value1", "--arg2", "value2");
		ParseResult result = parse("root4", "--arg1", "--arg2");
		assertThat(result).isNotNull();
		assertThat(result.getErrorResults()).isNotEmpty();
		assertThat(result.getErrorResults().get(0).getParserMessage().getCode()).isEqualTo(2001);
		assertThat(result.getErrorResults().get(0).getParserMessage().getType()).isEqualTo(ParserMessage.Type.ERROR);
		// "101E:(pos 0): Unrecognised option '--arg2'"
		// assertThat(result.getErrorResults().get(0).getMessage()).contains("xxx");
	}

}
