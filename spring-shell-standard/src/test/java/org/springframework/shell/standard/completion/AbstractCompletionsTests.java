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
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.shell.CommandRegistry;
import org.springframework.shell.ConfigurableCommandRegistry;
import org.springframework.shell.MethodTarget;
import org.springframework.shell.ParameterResolver;
import org.springframework.shell.standard.completion.AbstractCompletions.CommandModel;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class AbstractCompletionsTests {

	@Test
	public void testBasicModelGeneration() {
		DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
		ConfigurableCommandRegistry commandRegistry = new ConfigurableCommandRegistry();
		List<ParameterResolver> parameterResolvers = new ArrayList<>();

		TestCommands commands = new TestCommands();

		Method method1 = ReflectionUtils.findMethod(TestCommands.class, "test1");
		Method method2 = ReflectionUtils.findMethod(TestCommands.class, "test2");
		Method method3 = ReflectionUtils.findMethod(TestCommands.class, "test3");
		Method method4 = ReflectionUtils.findMethod(TestCommands.class, "test4");

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
		assertThat(commandModel.commands()).hasSize(3);
		assertThat(commandModel.commands().stream().map(c -> c.getMain())).containsExactlyInAnyOrder("test1", "test2",
				"test3");
		assertThat(commandModel.commands().stream().filter(c -> c.getMain().equals("test3")).findFirst().get()
				.subCommands()).hasSize(1);
	}

	private static class TestCompletions extends AbstractCompletions {

		public TestCompletions(ResourceLoader resourceLoader, CommandRegistry commandRegistry,
				List<ParameterResolver> parameterResolvers) {
			super(resourceLoader, commandRegistry, parameterResolvers);
		}

		CommandModel testCommandModel() {
			return generateCommandModel();
		}
	}

	private static class TestCommands {

		@SuppressWarnings("unused")
		void test1() {
		}

		@SuppressWarnings("unused")
		void test2() {
		}

		@SuppressWarnings("unused")
		void test3() {
		}

		@SuppressWarnings("unused")
		void test4() {
		}
	}
}
