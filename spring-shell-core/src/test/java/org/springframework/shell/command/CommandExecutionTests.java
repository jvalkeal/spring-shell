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

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.messaging.handler.annotation.support.HeaderMethodArgumentResolver;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;

import static org.assertj.core.api.Assertions.assertThat;

public class CommandExecutionTests extends AbstractCommandTests {

	private CommandExecution execution;

	@BeforeEach
	public void setupCommandExecutionTests() {
		List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();
		resolvers.add(new HeaderMethodArgumentResolver(new DefaultConversionService(), null));
		resolvers.add(new CommandContextMethodArgumentResolver());
		execution = CommandExecution.of(resolvers);
	}

	@Test
	public void testFunctionExecution() {
		CommandRegistration r1 = CommandRegistration.builder()
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
			.withTarget()
				.method(pojo1, "method3", String.class)
				.and()
			.build();
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
			.withTarget()
				.method(pojo1, "method1")
				.and()
			.build();
		execution.evaluate(r1, new String[]{"--arg1", "myarg1value"});
		assertThat(pojo1.method1Count).isEqualTo(1);
		assertThat(pojo1.method1Ctx).isNotNull();
	}

	@Test
	public void testMethodSinglePositionalArgs() {
		CommandRegistration r1 = CommandRegistration.builder()
			.command("command1")
			.help("help")
			.withOption()
				.longNames("arg1")
				.description("some arg1")
				.and()
			.withTarget()
				.method(pojo1, "method4")
				.and()
			.build();
		execution.evaluate(r1, new String[]{"myarg1value"});
		assertThat(pojo1.method4Count).isEqualTo(1);
		assertThat(pojo1.method4Arg1).isEqualTo("myarg1value");
	}

	@Test
	public void testMethodSingleWithNamedArgs() {
		CommandRegistration r1 = CommandRegistration.builder()
			.command("command1")
			.help("help")
			.withOption()
				.longNames("arg1")
				.and()
			.withTarget()
				.method(pojo1, "method4")
				.and()
			.build();
		Object result = execution.evaluate(r1, new String[]{"--arg1", "myarg1value"});
		assertThat(pojo1.method4Count).isEqualTo(1);
		assertThat(pojo1.method4Arg1).isEqualTo("myarg1value");
		assertThat(result).isEqualTo("himyarg1value");
	}

	@Test
	public void testMethodMultiPositionalArgs() {
		CommandRegistration r1 = CommandRegistration.builder()
			.command("command1")
			.help("help")
			.withOption()
				.longNames("arg1")
				.description("some arg1")
				.and()
			.withTarget()
				.method(pojo1, "method4")
				.and()
			.build();
		execution.evaluate(r1, new String[]{"myarg1value1", "myarg1value2"});
		assertThat(pojo1.method4Count).isEqualTo(1);
		assertThat(pojo1.method4Arg1).isEqualTo("myarg1value1");
	}

	@Test
	public void testMethodMultipleArgs() {
		CommandRegistration r1 = CommandRegistration.builder()
			.command("command1")
			.help("help")
			.withOption()
				.longNames("arg1")
				.description("some arg1")
				.and()
			.withOption()
				.longNames("arg2")
				.description("some arg2")
				.and()
			.withOption()
				.longNames("arg3")
				.description("some arg3")
				.and()
			.withTarget()
				.method(pojo1, "method6")
				.and()
			.build();

		execution.evaluate(r1, new String[]{"--arg1", "myarg1value", "--arg2", "myarg2value", "--arg3", "myarg3value"});
		assertThat(pojo1.method6Count).isEqualTo(1);
		assertThat(pojo1.method6Arg1).isEqualTo("myarg1value");
		assertThat(pojo1.method6Arg2).isEqualTo("myarg2value");
		assertThat(pojo1.method6Arg3).isEqualTo("myarg3value");
	}


	@Test
	public void testMethodMultipleIntArgs() {
		CommandRegistration r1 = CommandRegistration.builder()
			.command("command1")
			.help("help")
			.withOption()
				.longNames("arg1")
				.description("some arg1")
				.and()
			.withOption()
				.longNames("arg2")
				.description("some arg2")
				.and()
			.withOption()
				.longNames("arg3")
				.description("some arg3")
				.and()
			.withTarget()
				.method(pojo1, "method7")
				.and()
			.build();

		execution.evaluate(r1, new String[]{"--arg1", "1", "--arg2", "2", "--arg3", "3"});
		assertThat(pojo1.method7Count).isEqualTo(1);
		assertThat(pojo1.method7Arg1).isEqualTo(1);
		assertThat(pojo1.method7Arg2).isEqualTo(2);
		assertThat(pojo1.method7Arg3).isEqualTo(3);
	}

	@Test
	public void testMethodMultiplePositionalStringArgs() {
		CommandRegistration r1 = CommandRegistration.builder()
			.command("command1")
			.help("help")
			.withOption()
				.longNames("arg1")
				.description("some arg1")
				.and()
			.withOption()
				.longNames("arg2")
				.description("some arg2")
				.and()
			.withOption()
				.longNames("arg3")
				.description("some arg3")
				.and()
			.withTarget()
				.method(pojo1, "method6")
				.and()
			.build();

		execution.evaluate(r1, new String[]{"myarg1value", "myarg2value", "myarg3value"});
		assertThat(pojo1.method6Count).isEqualTo(1);
		assertThat(pojo1.method6Arg1).isEqualTo("myarg1value");
		assertThat(pojo1.method6Arg2).isEqualTo("myarg2value");
		assertThat(pojo1.method6Arg3).isEqualTo("myarg3value");
	}

	@Test
	public void testShortCombinedWithoutValue() {
		CommandRegistration r1 = CommandRegistration.builder()
			.command("command1")
			.help("help")
			.withOption()
				.shortNames('a')
				.description("short arg a")
				.type(boolean.class)
				.and()
			.withOption()
				.shortNames('b')
				.description("short arg b")
				.type(boolean.class)
				.and()
			.withOption()
				.shortNames('c')
				.description("short arg c")
				.type(boolean.class)
				.and()
			.withTarget()
				.method(pojo1, "method5")
				.and()
			.build();
		execution.evaluate(r1, new String[]{"-abc"});
		assertThat(pojo1.method5ArgA).isTrue();
		assertThat(pojo1.method5ArgB).isTrue();
		assertThat(pojo1.method5ArgC).isTrue();
	}

	@Test
	public void testShortCombinedSomeHavingValue() {
		CommandRegistration r1 = CommandRegistration.builder()
			.command("command1")
			.help("help")
			.withOption()
				.shortNames('a')
				.description("short arg a")
				.type(boolean.class)
				.and()
			.withOption()
				.shortNames('b')
				.description("short arg b")
				.type(boolean.class)
				.and()
			.withOption()
				.shortNames('c')
				.description("short arg c")
				.type(boolean.class)
				.and()
			.withTarget()
				.method(pojo1, "method5")
				.and()
			.build();
		execution.evaluate(r1, new String[]{"-ac", "-b", "false"});
		assertThat(pojo1.method5ArgA).isTrue();
		assertThat(pojo1.method5ArgB).isFalse();
		assertThat(pojo1.method5ArgC).isTrue();
	}
}
