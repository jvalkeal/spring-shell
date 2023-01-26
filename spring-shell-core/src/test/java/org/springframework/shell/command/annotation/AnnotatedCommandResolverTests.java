/*
 * Copyright 2022-2023 the original author or authors.
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
package org.springframework.shell.command.annotation;

import java.util.Collection;
import java.util.function.Supplier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.shell.command.CommandHandlingResult;
import org.springframework.shell.command.CommandRegistration;

import static org.assertj.core.api.Assertions.assertThat;

public class AnnotatedCommandResolverTests {

	private AnnotationConfigApplicationContext applicationContext;
	private Supplier<CommandRegistration.Builder> builder = () -> CommandRegistration.builder();

	@BeforeEach
	public void setup() {
	}

	@AfterEach
	public void cleanup() {
		if (applicationContext != null) {
			applicationContext.close();
		}
		applicationContext = null;
	}

	@Test
	void resolveCommandNoOptions() {
		applicationContext = new AnnotationConfigApplicationContext(CommandNoOptions.class);
		AnnotatedCommandResolver resolver = new AnnotatedCommandResolver(applicationContext, builder);
		Collection<CommandRegistration> registrations = resolver.resolve();
		assertThat(registrations).hasSize(1);
		assertThat(registrations.stream().findFirst().get().getCommand()).isEqualTo("command");
	}

	@CommandComponent
	static class CommandNoOptions {

		@Command
		String command() {
			return "hi";
		}
	}

	@Test
	void resolveCommandOptionWithOption() {
		applicationContext = new AnnotationConfigApplicationContext(CommandOptionWithOption.class);
		AnnotatedCommandResolver resolver = new AnnotatedCommandResolver(applicationContext, builder);
		Collection<CommandRegistration> registrations = resolver.resolve();
		assertThat(registrations).hasSize(1);
		CommandRegistration registration = registrations.stream().findFirst().get();
		assertThat(registration.getOptions()).hasSize(1);
	}

	@CommandComponent
	static class CommandOptionWithOption {

		@Command
		String command(String arg) {
			return "hi";
		}
	}

	@Test
	void definesGroup() {
		applicationContext = new AnnotationConfigApplicationContext(DefinesGroup.class);
		AnnotatedCommandResolver resolver = new AnnotatedCommandResolver(applicationContext, builder);
		Collection<CommandRegistration> registrations = resolver.resolve();
		assertThat(registrations).hasSize(1);
		CommandRegistration registration = registrations.stream().findFirst().get();
		assertThat(registration.getGroup()).isEqualTo("fakegroup");
	}

	@CommandComponent
	static class DefinesGroup {

		@Command(group = "fakegroup")
		String command() {
			return "hi";
		}
	}

	@Test
	void definesDesc() {
		applicationContext = new AnnotationConfigApplicationContext(DefinesDesc.class);
		AnnotatedCommandResolver resolver = new AnnotatedCommandResolver(applicationContext, builder);
		Collection<CommandRegistration> registrations = resolver.resolve();
		assertThat(registrations).hasSize(1);
		CommandRegistration registration = registrations.stream().findFirst().get();
		assertThat(registration.getDescription()).isEqualTo("fakedesc");
	}

	@CommandComponent
	static class DefinesDesc {

		@Command(description = "fakedesc")
		String command() {
			return "hi";
		}
	}

	@Test
	void resolveMultipleCommands() {
		applicationContext = new AnnotationConfigApplicationContext(CommandMultiple.class);
		AnnotatedCommandResolver resolver = new AnnotatedCommandResolver(applicationContext, builder);
		Collection<CommandRegistration> registrations = resolver.resolve();
		assertThat(registrations).hasSize(2);
	}

	@CommandComponent
	static class CommandMultiple {

		@Command
		String command1() {
			return "hi";
		}

		@Command
		String command2() {
			return "hi";
		}
	}

	@Test
	void resolveHiddenCommand() {
		applicationContext = new AnnotationConfigApplicationContext(HiddenCommand.class);
		AnnotatedCommandResolver resolver = new AnnotatedCommandResolver(applicationContext, builder);
		Collection<CommandRegistration> registrations = resolver.resolve();
		assertThat(registrations).hasSize(1);
		CommandRegistration registration = registrations.stream().findFirst().get();
		assertThat(registration.isHidden()).isTrue();
	}

	@CommandComponent
	static class HiddenCommand {

		@Command(hidden = true)
		String command() {
			return "hi";
		}
	}

	@Test
	void resolvePlainExceptionHandling() {
		applicationContext = new AnnotationConfigApplicationContext(CommandWithErrorHandling.class);
		AnnotatedCommandResolver resolver = new AnnotatedCommandResolver(applicationContext, builder);
		Collection<CommandRegistration> registrations = resolver.resolve();
		CommandRegistration registration = registrations.stream().findFirst().get();
		assertThat(registration.getExceptionResolvers()).hasSize(1);
	}

	@CommandComponent
	static class CommandWithErrorHandling {

		@Command
		String command() {
			throw new CustomException1();
		}

		@ExceptionResolver({ CustomException1.class })
		CommandHandlingResult errorHandler(Exception e) {
			return CommandHandlingResult.of("Hi, handled exception\n", 42);
		}

		private static class CustomException1 extends RuntimeException {
		}
	}

}
