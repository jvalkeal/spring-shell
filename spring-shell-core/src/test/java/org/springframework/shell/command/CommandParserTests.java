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
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.core.ResolvableType;
import org.springframework.shell.command.CommandParser.Results;

import static org.assertj.core.api.Assertions.assertThat;

public class CommandParserTests extends AbstractCommandTests {

	private CommandParser parser;

	@BeforeEach
	public void setupCommandParserTests() {
		parser = CommandParser.of();
	}

	@Test
	public void testEmptyOptionsAndArgs() {
		Results results = parser.parse(Collections.emptyList(), new String[0]);
		assertThat(results.results()).hasSize(0);
	}

	@Test
	public void testLongName() {
		CommandOption option1 = longOption("arg1");
		CommandOption option2 = longOption("arg2");
		List<CommandOption> options = Arrays.asList(option1, option2);
		String[] args = new String[]{"--arg1", "foo"};
		Results results = parser.parse(options, args);
		assertThat(results.results()).hasSize(1);
		assertThat(results.results().get(0).option()).isSameAs(option1);
		assertThat(results.results().get(0).value()).isEqualTo("foo");
	}

	@Test
	public void testShortName() {
		CommandOption option1 = shortOption('a');
		CommandOption option2 = shortOption('b');
		List<CommandOption> options = Arrays.asList(option1, option2);
		String[] args = new String[]{"-a", "foo"};
		Results results = parser.parse(options, args);
		assertThat(results.results()).hasSize(1);
		assertThat(results.results().get(0).option()).isSameAs(option1);
		assertThat(results.results().get(0).value()).isEqualTo("foo");
	}

	@Test
	public void testMultipleArgs() {
		CommandOption option1 = longOption("arg1");
		CommandOption option2 = longOption("arg2");
		List<CommandOption> options = Arrays.asList(option1, option2);
		String[] args = new String[]{"--arg1", "foo", "--arg2", "bar"};
		Results results = parser.parse(options, args);
		assertThat(results.results()).hasSize(2);
		assertThat(results.results().get(0).option()).isSameAs(option1);
		assertThat(results.results().get(0).value()).isEqualTo("foo");
		assertThat(results.results().get(1).option()).isSameAs(option2);
		assertThat(results.results().get(1).value()).isEqualTo("bar");
	}

	@Test
	public void testBooleanWithoutArg() {
		ResolvableType type = ResolvableType.forType(boolean.class);
		CommandOption option1 = shortOption('v', type);
		List<CommandOption> options = Arrays.asList(option1);
		String[] args = new String[]{"-v"};
		Results results = parser.parse(options, args);
		assertThat(results.results()).hasSize(1);
		assertThat(results.results().get(0).option()).isSameAs(option1);
		assertThat(results.results().get(0).value()).isEqualTo(true);
	}

	@Test
	public void testBooleanWithArg() {
		ResolvableType type = ResolvableType.forType(boolean.class);
		CommandOption option1 = shortOption('v', type);
		List<CommandOption> options = Arrays.asList(option1);
		String[] args = new String[]{"-v", "false"};
		Results results = parser.parse(options, args);
		assertThat(results.results()).hasSize(1);
		assertThat(results.results().get(0).option()).isSameAs(option1);
		assertThat(results.results().get(0).value()).isEqualTo(false);
	}

	@Test
	public void testMissingRequiredOption() {
		CommandOption option1 = longOption("arg1", true);
		List<CommandOption> options = Arrays.asList(option1);
		String[] args = new String[]{};
		Results results = parser.parse(options, args);
		assertThat(results.errors()).hasSize(1);
	}

	@Test
	public void testSpaceInArgWithOneArg() {
		CommandOption option1 = longOption("arg1");
		List<CommandOption> options = Arrays.asList(option1);
		String[] args = new String[]{"--arg1", "foo bar"};
		Results results = parser.parse(options, args);
		assertThat(results.results()).hasSize(1);
		assertThat(results.results().get(0).option()).isSameAs(option1);
		assertThat(results.results().get(0).value()).isEqualTo("foo bar");
	}

	@Test
	public void testSpaceInArgWithMultipleArgs() {
		CommandOption option1 = longOption("arg1");
		CommandOption option2 = longOption("arg2");
		List<CommandOption> options = Arrays.asList(option1, option2);
		String[] args = new String[]{"--arg1", "foo bar", "--arg2", "hi"};
		Results results = parser.parse(options, args);
		assertThat(results.results()).hasSize(2);
		assertThat(results.results().get(0).option()).isSameAs(option1);
		assertThat(results.results().get(0).value()).isEqualTo("foo bar");
		assertThat(results.results().get(1).option()).isSameAs(option2);
		assertThat(results.results().get(1).value()).isEqualTo("hi");
	}

	@Test
	public void testNonMappedArgs() {
		String[] args = new String[]{"arg1", "arg2"};
		Results results = parser.parse(Collections.emptyList(), args);
		assertThat(results.results()).hasSize(0);
		assertThat(results.positional()).containsExactly("arg1", "arg2");
	}

	@Test
	public void testShortOptionsCombined() {
		CommandOption optionA = shortOption('a');
		CommandOption optionB = shortOption('b');
		CommandOption optionC = shortOption('c');
		List<CommandOption> options = Arrays.asList(optionA, optionB, optionC);
		String[] args = new String[]{"-abc"};

		Results results = parser.parse(options, args);
		assertThat(results.results()).hasSize(3);
		assertThat(results.results().get(0).option()).isSameAs(optionA);
		assertThat(results.results().get(1).option()).isSameAs(optionB);
		assertThat(results.results().get(2).option()).isSameAs(optionC);
		assertThat(results.results().get(0).value()).isNull();
		assertThat(results.results().get(1).value()).isNull();
		assertThat(results.results().get(2).value()).isNull();
	}

	@Test
	public void testShortOptionsCombinedBooleanType() {
		CommandOption optionA = shortOption('a', ResolvableType.forType(boolean.class));
		CommandOption optionB = shortOption('b', ResolvableType.forType(boolean.class));
		CommandOption optionC = shortOption('c', ResolvableType.forType(boolean.class));
		List<CommandOption> options = Arrays.asList(optionA, optionB, optionC);
		String[] args = new String[]{"-abc"};

		Results results = parser.parse(options, args);
		assertThat(results.results()).hasSize(3);
		assertThat(results.results().get(0).option()).isSameAs(optionA);
		assertThat(results.results().get(1).option()).isSameAs(optionB);
		assertThat(results.results().get(2).option()).isSameAs(optionC);
		assertThat(results.results().get(0).value()).isEqualTo(true);
		assertThat(results.results().get(1).value()).isEqualTo(true);
		assertThat(results.results().get(2).value()).isEqualTo(true);
	}

	@Test
	public void testShortOptionsCombinedBooleanTypeArgFalse() {
		CommandOption optionA = shortOption('a', ResolvableType.forType(boolean.class));
		CommandOption optionB = shortOption('b', ResolvableType.forType(boolean.class));
		CommandOption optionC = shortOption('c', ResolvableType.forType(boolean.class));
		List<CommandOption> options = Arrays.asList(optionA, optionB, optionC);
		String[] args = new String[]{"-abc", "false"};

		Results results = parser.parse(options, args);
		assertThat(results.results()).hasSize(3);
		assertThat(results.results().get(0).option()).isSameAs(optionA);
		assertThat(results.results().get(1).option()).isSameAs(optionB);
		assertThat(results.results().get(2).option()).isSameAs(optionC);
		assertThat(results.results().get(0).value()).isEqualTo(false);
		assertThat(results.results().get(1).value()).isEqualTo(false);
		assertThat(results.results().get(2).value()).isEqualTo(false);
	}

	@Test
	public void testShortOptionsCombinedBooleanTypeSomeArgFalse() {
		CommandOption optionA = shortOption('a', ResolvableType.forType(boolean.class));
		CommandOption optionB = shortOption('b', ResolvableType.forType(boolean.class));
		CommandOption optionC = shortOption('c', ResolvableType.forType(boolean.class));
		List<CommandOption> options = Arrays.asList(optionA, optionB, optionC);
		String[] args = new String[]{"-ac", "-b", "false"};

		Results results = parser.parse(options, args);
		assertThat(results.results()).hasSize(3);
		assertThat(results.results().get(0).option()).isSameAs(optionA);
		assertThat(results.results().get(1).option()).isSameAs(optionC);
		assertThat(results.results().get(2).option()).isSameAs(optionB);
		assertThat(results.results().get(0).value()).isEqualTo(true);
		assertThat(results.results().get(1).value()).isEqualTo(true);
		assertThat(results.results().get(2).value()).isEqualTo(false);
	}

	@Test
	public void testLongOptionsWithArray() {
		CommandOption option1 = longOption("arg1", ResolvableType.forType(int[].class));
		List<CommandOption> options = Arrays.asList(option1);
		String[] args = new String[]{"--arg1", "1", "2"};
		Results results = parser.parse(options, args);
		assertThat(results.results()).hasSize(1);
		assertThat(results.results().get(0).option()).isSameAs(option1);
		assertThat(results.results().get(0).value()).isEqualTo(new String[] { "1", "2" });
	}

	private static CommandOption longOption(String name) {
		return longOption(name, null);
	}

	private static CommandOption longOption(String name, boolean required) {
		return longOption(name, null, required);
	}

	private static CommandOption longOption(String name, ResolvableType type) {
		return longOption(name, type, false);
	}

	private static CommandOption longOption(String name, ResolvableType type, boolean required) {
		return CommandOption.of(new String[] { name }, new Character[0], "desc", type, required, null);
	}

	private static CommandOption shortOption(char name) {
		return shortOption(name, null);
	}

	private static CommandOption shortOption(char name, ResolvableType type) {
		return CommandOption.of(new String[0], new Character[] { name }, "desc", type);
	}
}
