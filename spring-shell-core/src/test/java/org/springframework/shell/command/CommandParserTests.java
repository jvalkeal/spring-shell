/*
 * Copyright 2022 the original author or authors.
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
package org.springframework.shell.command;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.shell.command.CommandParser.Results;

import static org.assertj.core.api.Assertions.assertThat;

public class CommandParserTests extends AbstractCommandTests {

	private CommandParser parser;

	@BeforeEach
	public void setupCommandParserTests() {
		parser = CommandParser.of();
	}

	@Test
	public void testLongName() {
		CommandOption option1 = CommandOption.of(new String[]{"arg1"}, new Character[0], "desc");
		CommandOption option2 = CommandOption.of(new String[]{"arg2"}, new Character[0], "desc");
		List<CommandOption> options = Arrays.asList(option1, option2);
		String[] args = new String[]{"--arg1", "foo"};
		Results results = parser.parse(options, args);
		assertThat(results.results()).hasSize(1);
		assertThat(results.results().get(0).option()).isSameAs(option1);
		assertThat(results.results().get(0).value()).isEqualTo("foo");
	}

	@Test
	public void testShortName() {
		CommandOption option1 = CommandOption.of(new String[0], new Character[]{'a'}, "desc");
		CommandOption option2 = CommandOption.of(new String[0], new Character[]{'b'}, "desc");
		List<CommandOption> options = Arrays.asList(option1, option2);
		String[] args = new String[]{"-a", "foo"};
		Results results = parser.parse(options, args);
		assertThat(results.results()).hasSize(1);
		assertThat(results.results().get(0).option()).isSameAs(option1);
		assertThat(results.results().get(0).value()).isEqualTo("foo");
	}
}
