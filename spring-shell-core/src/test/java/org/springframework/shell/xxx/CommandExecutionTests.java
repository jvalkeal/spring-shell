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

import java.util.function.Function;

import org.junit.jupiter.api.Test;

import org.springframework.core.ResolvableType;

import static org.assertj.core.api.Assertions.assertThat;

public class CommandExecutionTests {

	private Pojo1 pojo1 = new Pojo1();
	private Function<CommandExecutionContext, String> function1 = ctx -> {
		String arg1 = ctx.getOptionValue("--arg1");
		return "hi" + arg1;
	};

	@Test
	public void testSimpleFunctionExecution() {
		CommandRegistration r1 = CommandRegistration.builder()
			.command("command1")
			.help("help")
			.withOption()
				.name("--arg1")
				.type(ResolvableType.forClass(String.class))
				.description("some arg1")
				.and()
			.withAction()
				.function(function1)
				.and()
			.build();
		CommandExecution execution = CommandExecution.of();
		Object result = execution.evaluate(r1, new String[]{"--arg1", "myarg1value"});
		assertThat(result).isEqualTo("himyarg1value");
	}

	@Test
	public void testMethodExecution() {
		CommandRegistration r1 = CommandRegistration.builder()
			.command("command1")
			.help("help")
			.withOption()
				.name("--arg1")
				.type(ResolvableType.forClass(String.class))
				.description("some arg1")
				.and()
			.withAction()
				.method(pojo1, "method3")
				.and()
			.build();
		CommandExecution execution = CommandExecution.of();
		Object result = execution.evaluate(r1, new String[]{"--arg1", "myarg1value"});
		assertThat(result).isEqualTo("himyarg1value");
	}

	private static class Pojo1 {

		public void method1() {
		}

		public String method2() {
			return null;
		}

		public void method3(String arg1) {
		}

		public String method4(String arg1) {
			return null;
		}
	}

}
