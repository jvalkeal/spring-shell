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
package org.springframework.shell.standard.completion;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupString;

import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.shell.CommandRegistry;
import org.springframework.shell.ConfigurableCommandRegistry;
import org.springframework.shell.MethodTarget;
import org.springframework.shell.ParameterResolver;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.standard.StandardParameterResolver;
import org.springframework.shell.standard.completion.AbstractCompletions.Builder;
import org.springframework.shell.standard.completion.AbstractCompletions.CommandModel;
import org.springframework.shell.standard.completion.AbstractCompletions.CommandModelCommand;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class AbstractCompletionsTests {

	@Test
	public void testBasicModelGeneration() {
		DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
		ConfigurableCommandRegistry commandRegistry = new ConfigurableCommandRegistry();
		List<ParameterResolver> parameterResolvers = new ArrayList<>();
		StandardParameterResolver resolver = new StandardParameterResolver(new DefaultConversionService(),
				Collections.emptySet());
		parameterResolvers.add(resolver);

		TestCommands commands = new TestCommands();

		Method method1 = ReflectionUtils.findMethod(TestCommands.class, "test1", String.class);
		Method method2 = ReflectionUtils.findMethod(TestCommands.class, "test2");
		Method method3 = ReflectionUtils.findMethod(TestCommands.class, "test3");
		Method method4 = ReflectionUtils.findMethod(TestCommands.class, "test4", String.class);

		MethodTarget methodTarget1 = new MethodTarget(method1, commands, "help");
		MethodTarget methodTarget2 = new MethodTarget(method2, commands, "help");
		MethodTarget methodTarget3 = new MethodTarget(method3, commands, "help");
		MethodTarget methodTarget4 = new MethodTarget(method4, commands, "help");

		commandRegistry.register("test1", methodTarget1);
		commandRegistry.register("test2", methodTarget2);
		commandRegistry.register("test3", methodTarget3);
		commandRegistry.register("test3 test4", methodTarget4);

		TestCompletions completions = new TestCompletions(resourceLoader, commandRegistry, parameterResolvers);
		CommandModel commandModel = completions.testCommandModel();
		assertThat(commandModel.getCommands()).hasSize(3);
		assertThat(commandModel.getCommands().stream().map(c -> c.getMain())).containsExactlyInAnyOrder("test1", "test2",
				"test3");
		assertThat(commandModel.getCommands().stream().filter(c -> c.getMain().equals("test1")).findFirst().get()
				.options()).hasSize(1);
		assertThat(commandModel.getCommands().stream().filter(c -> c.getMain().equals("test1")).findFirst().get()
				.options().get(0).option()).isEqualTo("--param1");
		assertThat(commandModel.getCommands().stream().filter(c -> c.getMain().equals("test2")).findFirst().get()
				.options()).hasSize(0);
		assertThat(commandModel.getCommands().stream().filter(c -> c.getMain().equals("test3")).findFirst().get()
				.options()).hasSize(0);
		assertThat(commandModel.getCommands().stream().filter(c -> c.getMain().equals("test3")).findFirst().get()
				.subCommands()).hasSize(1);
		assertThat(commandModel.getCommands().stream().filter(c -> c.getMain().equals("test3")).findFirst().get()
				.subCommands().get(0).getMain()).isEqualTo("test4");
		assertThat(commandModel.getCommands().stream().filter(c -> c.getMain().equals("test3")).findFirst().get()
				.subCommands().get(0).options()).hasSize(1);
		assertThat(commandModel.getCommands().stream().filter(c -> c.getMain().equals("test3")).findFirst().get()
				.subCommands().get(0).options().get(0).option()).isEqualTo("--param4");
	}

	@Test
	public void testBuilder() {
		DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
		ConfigurableCommandRegistry commandRegistry = new ConfigurableCommandRegistry();
		List<ParameterResolver> parameterResolvers = new ArrayList<>();
		StandardParameterResolver resolver = new StandardParameterResolver(new DefaultConversionService(),
				Collections.emptySet());
		parameterResolvers.add(resolver);

		TestCommands commands = new TestCommands();

		Method method1 = ReflectionUtils.findMethod(TestCommands.class, "test1", String.class);
		Method method2 = ReflectionUtils.findMethod(TestCommands.class, "test2");
		Method method3 = ReflectionUtils.findMethod(TestCommands.class, "test3");
		Method method4 = ReflectionUtils.findMethod(TestCommands.class, "test4", String.class);

		MethodTarget methodTarget1 = new MethodTarget(method1, commands, "help");
		MethodTarget methodTarget2 = new MethodTarget(method2, commands, "help");
		MethodTarget methodTarget3 = new MethodTarget(method3, commands, "help");
		MethodTarget methodTarget4 = new MethodTarget(method4, commands, "help");

		commandRegistry.register("test1", methodTarget1);
		commandRegistry.register("test2", methodTarget2);
		commandRegistry.register("test3", methodTarget3);
		commandRegistry.register("test3 test4", methodTarget4);

		TestCompletions completions = new TestCompletions(resourceLoader, commandRegistry, parameterResolvers);
		CommandModel commandModel = completions.testCommandModel();

		// List<CommandModelCommand> topcommands = commandModel.commands().stream()
		// 		.collect(Collectors.toList());
		// List<CommandModelCommand> subcommands = topcommands.stream()
		// 		.flatMap(c -> flatten(c))
		// 		.collect(Collectors.toList());

		// String groupStr1 =
		// 	"a(x) ::= <<foo><b(x)>>\n" +
		// 	"b(y) ::= <y>\n";

		// STGroup group1 = new STGroupString(groupStr1);

		Builder builder = completions.testBuilder();

		// List<String> subcommands = Arrays.asList("command1", "command2");

		String build = builder
				.withDefaultAttribute("name", "command")
				.withDefaultAttribute("model", commandModel)
				// .withDefaultMultiAttribute("subcommands", subcommands)
				// .withDefaultMultiAttribute("topcommands", topcommands)
				.setGroup("classpath:completion/bash.stg")
				.appendGroupInstance("main")
				.build();

		System.out.println(build);

	}

	// private Stream<CommandModelCommand> flatten(CommandModelCommand command) {
	// 	return Stream.concat(Stream.of(command), command.subCommands().stream().flatMap(c -> flatten(c)));
	// }

	private static class TestCompletions extends AbstractCompletions {

		public TestCompletions(ResourceLoader resourceLoader, CommandRegistry commandRegistry,
				List<ParameterResolver> parameterResolvers) {
			super(resourceLoader, commandRegistry, parameterResolvers);
		}

		CommandModel testCommandModel() {
			return generateCommandModel();
		}

		Builder testBuilder() {
			return super.builder();
		}
	}

	private static class TestCommands {

		@ShellMethod
		void test1(@ShellOption String param1) {
		}

		@ShellMethod
		void test2() {
		}

		@ShellMethod
		void test3() {
		}

		@ShellMethod
		void test4(@ShellOption String param4) {
		}
	}
}
