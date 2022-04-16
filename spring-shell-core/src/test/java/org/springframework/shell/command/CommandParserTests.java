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

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.core.ResolvableType;
import org.springframework.shell.command.CommandParser.MissingOptionException;
import org.springframework.shell.command.CommandParser.Results;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
		assertThatThrownBy(() -> {
			parser.parse(options, args);
		})
			.isInstanceOf(MissingOptionException.class)
			.extracting("options", InstanceOfAssertFactories.LIST).containsExactly(option1);
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
		return CommandOption.of(new String[] { name }, new Character[0], "desc", type, required);
	}

	private static CommandOption shortOption(char name) {
		return shortOption(name, null);
	}

	private static CommandOption shortOption(char name, ResolvableType type) {
		return CommandOption.of(new String[0], new Character[] { name }, "desc", type);
	}
}
