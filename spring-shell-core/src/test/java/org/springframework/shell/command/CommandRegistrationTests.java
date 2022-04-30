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

import org.springframework.core.ResolvableType;
import org.springframework.shell.command.CommandRegistration.TargetInfo.TargetType;
import org.springframework.shell.context.InteractionMode;

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
	public void testBasics() {
		CommandRegistration registration = CommandRegistration.builder()
			.command("command1")
			.withTarget()
				.function(function1)
				.and()
			.build();
		assertThat(registration.getCommands()).containsExactly("command1");
		assertThat(registration.getGroup()).isNull();
		assertThat(registration.getInteractionMode()).isEqualTo(InteractionMode.ALL);

		registration = CommandRegistration.builder()
			.command("command1")
			.interactionMode(InteractionMode.NONINTERACTIVE)
			.group("fakegroup")
			.withTarget()
				.function(function1)
				.and()
			.build();
		assertThat(registration.getInteractionMode()).isEqualTo(InteractionMode.NONINTERACTIVE);
		assertThat(registration.getGroup()).isEqualTo("fakegroup");
	}

	@Test
	public void testCommandStructures() {
		CommandRegistration registration = CommandRegistration.builder()
			.command("command1")
			.withTarget()
				.function(function1)
				.and()
			.build();
		assertThat(registration.getCommands()).containsExactly("command1");

		registration = CommandRegistration.builder()
			.command("command1", "command2")
			.withTarget()
				.function(function1)
				.and()
			.build();
		assertThat(registration.getCommands()).containsExactly("command1", "command2");

		registration = CommandRegistration.builder()
			.command("command1 command2")
			.withTarget()
				.function(function1)
				.and()
			.build();
		assertThat(registration.getCommands()).containsExactly("command1", "command2");

		registration = CommandRegistration.builder()
			.command(" command1  command2 ")
			.withTarget()
				.function(function1)
				.and()
			.build();
		assertThat(registration.getCommands()).containsExactly("command1", "command2");
	}

	@Test
	public void testFunctionRegistration() {
		CommandRegistration registration = CommandRegistration.builder()
			.command("command1")
			.withTarget()
				.function(function1)
				.and()
			.build();
		assertThat(registration.getTarget().getTargetType()).isEqualTo(TargetType.FUNCTION);
		assertThat(registration.getTarget().getFunction()).isNotNull();
		assertThat(registration.getTarget().getBean()).isNull();
		assertThat(registration.getTarget().getMethod()).isNull();
	}

	@Test
	public void testMethodRegistration() {
		CommandRegistration registration = CommandRegistration.builder()
			.command("command1")
			.withTarget()
				.method(pojo1, "method3", String.class)
				.and()
			.build();
		assertThat(registration.getTarget().getTargetType()).isEqualTo(TargetType.METHOD);
		assertThat(registration.getTarget().getFunction()).isNull();
		assertThat(registration.getTarget().getBean()).isNotNull();
		assertThat(registration.getTarget().getMethod()).isNotNull();
	}

	@Test
	public void testCanUseOnlyOneTarget() {
		assertThatThrownBy(() -> {
			CommandRegistration.builder()
				.command("command1")
				.withTarget()
					.method(pojo1, "method3", String.class)
					.function(function1)
					.and()
				.build();
		}).isInstanceOf(IllegalStateException.class).hasMessageContaining("only one target can exist");
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
			.withTarget()
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
			.withTarget()
				.method(pojo1, "method3", String.class)
				.and()
			.build();
		assertThat(registration.getCommands()).containsExactly("command1");
		assertThat(registration.getHelp()).isEqualTo("help");
		assertThat(registration.getOptions()).hasSize(1);
	}

	@Test
	public void testOptionWithType() {
		CommandRegistration registration = CommandRegistration.builder()
			.command("command1")
			.help("help")
			.withOption()
				.shortNames('v')
				.type(boolean.class)
				.description("some arg1")
				.and()
			.withTarget()
				.function(function1)
				.and()
			.build();
		assertThat(registration.getCommands()).containsExactly("command1");
		assertThat(registration.getHelp()).isEqualTo("help");
		assertThat(registration.getOptions()).hasSize(1);
		assertThat(registration.getOptions().get(0).getShortNames()).containsExactly('v');
		assertThat(registration.getOptions().get(0).getType()).isEqualTo(ResolvableType.forType(boolean.class));
	}

	@Test
	public void testOptionWithRequired() {
		CommandRegistration registration = CommandRegistration.builder()
			.command("command1")
			.help("help")
			.withOption()
				.shortNames('v')
				.type(boolean.class)
				.description("some arg1")
				.required(true)
				.and()
			.withTarget()
				.function(function1)
				.and()
			.build();
		assertThat(registration.getCommands()).containsExactly("command1");
		assertThat(registration.getHelp()).isEqualTo("help");
		assertThat(registration.getOptions()).hasSize(1);
		assertThat(registration.getOptions().get(0).getShortNames()).containsExactly('v');
		assertThat(registration.getOptions().get(0).isRequired()).isTrue();

		registration = CommandRegistration.builder()
			.command("command1")
			.help("help")
			.withOption()
				.shortNames('v')
				.type(boolean.class)
				.description("some arg1")
				.required(false)
				.and()
			.withTarget()
				.function(function1)
				.and()
			.build();

		assertThat(registration.getOptions()).hasSize(1);
		assertThat(registration.getOptions().get(0).isRequired()).isFalse();

		registration = CommandRegistration.builder()
			.command("command1")
			.help("help")
			.withOption()
				.shortNames('v')
				.type(boolean.class)
				.description("some arg1")
				.and()
			.withTarget()
				.function(function1)
				.and()
			.build();

		assertThat(registration.getOptions()).hasSize(1);
		assertThat(registration.getOptions().get(0).isRequired()).isFalse();

		registration = CommandRegistration.builder()
			.command("command1")
			.help("help")
			.withOption()
				.shortNames('v')
				.type(boolean.class)
				.description("some arg1")
				.required()
				.and()
			.withTarget()
				.function(function1)
				.and()
			.build();

		assertThat(registration.getOptions()).hasSize(1);
		assertThat(registration.getOptions().get(0).isRequired()).isTrue();
	}

	@Test
	public void testOptionWithDefaultValue() {
		CommandRegistration registration = CommandRegistration.builder()
			.command("command1")
			.help("help")
			.withOption()
				.shortNames('v')
				.type(boolean.class)
				.description("some arg1")
				.defaultValue("defaultValue")
				.and()
			.withTarget()
				.function(function1)
				.and()
			.build();
		assertThat(registration.getCommands()).containsExactly("command1");
		assertThat(registration.getHelp()).isEqualTo("help");
		assertThat(registration.getOptions()).hasSize(1);
		assertThat(registration.getOptions().get(0).getDefaultValue()).isEqualTo("defaultValue");
	}

	@Test
	public void testOptionWithPositionValue() {
		CommandRegistration registration = CommandRegistration.builder()
			.command("command1")
			.withOption()
				.longNames("arg1")
				.position(1)
				.and()
			.withOption()
				.longNames("arg2")
				.and()
			.withTarget()
				.function(function1)
				.and()
			.build();
		assertThat(registration.getCommands()).containsExactly("command1");
		assertThat(registration.getOptions()).hasSize(2);
		assertThat(registration.getOptions().get(0).getPosition()).isEqualTo(1);
		assertThat(registration.getOptions().get(1).getPosition()).isEqualTo(-1);
	}
}
