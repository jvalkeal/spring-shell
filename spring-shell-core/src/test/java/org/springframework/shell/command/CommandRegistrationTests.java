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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CommandRegistrationTests extends AbstractCommandTests {

	@Test
	public void testCommandMustBeSet() {
		assertThatThrownBy(() -> {
			CommandRegistration.builder().build();
		}).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("command cannot be empty");
	}

	@Test
	public void testCommandStructures() {
		CommandRegistration registration = CommandRegistration.builder()
			.command("command1")
			.targetFunction()
				.function(function1)
				.and()
			.build();
		assertThat(registration.getCommands()).containsExactly("command1");

		registration = CommandRegistration.builder()
			.command("command1", "command2")
			.targetFunction()
				.function(function1)
				.and()
			.build();
		assertThat(registration.getCommands()).containsExactly("command1", "command2");

		registration = CommandRegistration.builder()
			.command("command1 command2")
			.targetFunction()
				.function(function1)
				.and()
			.build();
		assertThat(registration.getCommands()).containsExactly("command1", "command2");

		registration = CommandRegistration.builder()
			.command(" command1  command2 ")
			.targetFunction()
				.function(function1)
				.and()
			.build();
		assertThat(registration.getCommands()).containsExactly("command1", "command2");
	}

	@Test
	public void testFunctionRegistration() {
		CommandRegistration registration = CommandRegistration.builder()
			.command("command1")
			.targetFunction()
				.function(function1)
				.and()
			.build();
		assertThat(registration.getFunction()).isNotNull();
		assertThat(registration.getMethod()).isNull();
	}

	@Test
	public void testMethodRegistration() {
		CommandRegistration registration = CommandRegistration.builder()
			.command("command1")
			.targetMethod()
				.method(pojo1, "method3", String.class)
				.and()
			.build();
		assertThat(registration.getFunction()).isNull();
		assertThat(registration.getMethod()).isNotNull();
	}

	@Test
	public void testCanUseOnlyOneTarget() {
		assertThatThrownBy(() -> {
			CommandRegistration.builder()
				.command("command1")
				.targetFunction()
					.function(function1)
					.and()
				.targetMethod()
					.method(pojo1, "method3", String.class)
					.and()
				.build();
		}).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("only one target can exist");
	}

	@Test
	public void testSimpleFullRegistrationWithFunction() {
		CommandRegistration registration = CommandRegistration.builder()
			.command("command1")
			.help("help")
			.withOption()
				.longNames("arg1")
				.description("some arg1")
				.and()
			.targetFunction()
				.function(function1)
				.and()
			.build();
		assertThat(registration.getCommands()).containsExactly("command1");
		assertThat(registration.getHelp()).isEqualTo("help");
		assertThat(registration.getOptions()).hasSize(1);
		assertThat(registration.getOptions().get(0).getLongNames()).containsExactly("arg1");
	}

	@Test
	public void testSimpleFullRegistrationWithMethod() {
		CommandRegistration registration = CommandRegistration.builder()
			.command("command1")
			.help("help")
			.withOption()
				.longNames("arg1")
				.description("some arg1")
				.and()
			.targetMethod()
				.method(pojo1, "method3", String.class)
				.and()
			.build();
		assertThat(registration.getCommands()).containsExactly("command1");
		assertThat(registration.getHelp()).isEqualTo("help");
		assertThat(registration.getOptions()).hasSize(1);
	}
}
