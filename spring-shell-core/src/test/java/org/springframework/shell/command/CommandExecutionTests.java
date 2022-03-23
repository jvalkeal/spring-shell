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

public class CommandExecutionTests extends AbstractCommandTests {

	@Test
	public void testFunctionExecution() {
		CommandRegistration r1 = CommandRegistration.builder()
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
		CommandExecution execution = CommandExecution.of();
		Object result = execution.evaluate(r1, new String[]{"--arg1", "myarg1value"});
		assertThat(result).isEqualTo("himyarg1value");
	}

	@Test
	public void testMethodExecution1() {
		CommandRegistration r1 = CommandRegistration.builder()
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
		CommandExecution execution = CommandExecution.of();
		Object result = execution.evaluate(r1, new String[]{"--arg1", "myarg1value"});
		assertThat(result).isEqualTo("himyarg1value");
		assertThat(pojo1.method3Count).isEqualTo(1);
	}

	@Test
	public void testMethodExecution2() {
		CommandRegistration r1 = CommandRegistration.builder()
			.command("command1")
			.help("help")
			.withOption()
				.longNames("arg1")
				.description("some arg1")
				.and()
			.targetMethod()
				.method(pojo1, "method1")
				.and()
			.build();
		CommandExecution execution = CommandExecution.of();
		execution.evaluate(r1, new String[]{"--arg1", "myarg1value"});
		assertThat(pojo1.method1Count).isEqualTo(1);
		assertThat(pojo1.method1Ctx).isNotNull();
	}

}
