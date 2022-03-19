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
package org.springframework.shell.xxx;

import org.junit.jupiter.api.Test;

import org.springframework.core.ResolvableType;

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
			.build();
		assertThat(registration.getCommands()).containsExactly("command1");

		registration = CommandRegistration.builder()
			.command("command1", "command2")
			.build();
		assertThat(registration.getCommands()).containsExactly("command1", "command2");

		registration = CommandRegistration.builder()
			.command("command1 command2")
			.build();
		assertThat(registration.getCommands()).containsExactly("command1", "command2");

		registration = CommandRegistration.builder()
			.command(" command1  command2 ")
			.build();
		assertThat(registration.getCommands()).containsExactly("command1", "command2");
	}

	@Test
	public void testSimpleFullRegistration() {
		CommandRegistration registration = CommandRegistration.builder()
			.command("command1")
			.help("help")
			.withOption()
				.name("--arg1")
				// .type(ResolvableType.forClass(String.class))
				.description("some arg1")
				.and()
			.withAction()
				.function(function1)
				.and()
			.build();
		assertThat(registration.getCommands()).containsExactly("command1");
		assertThat(registration.getHelp()).isEqualTo("help");
		assertThat(registration.getOptions()).hasSize(1);
		assertThat(registration.getOptions().get(0).getName()).isEqualTo("arg1");
		assertThat(registration.getOptions().get(0).getAliases()).containsExactly("--arg1");
	}
}
