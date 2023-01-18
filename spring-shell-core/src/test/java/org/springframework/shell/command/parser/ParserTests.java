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
	}
}
