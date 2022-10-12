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

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.shell.command.CommandExecution.CommandParserExceptionsException;
import org.springframework.shell.command.CommandParser.CommandParserException;
import org.springframework.shell.command.CommandParser.MissingOptionException;
import org.springframework.shell.command.CommandRegistration;

import static org.assertj.core.api.Assertions.assertThat;

class CommandParserExceptionResolverTests {

	@Test
	void resolvesMissingOption() {
		CommandRegistration registration = CommandRegistration.builder()
			.command("required-value")
			.withOption()
				.longNames("arg1")
				.description("Desc arg1")
				.required()
				.and()
			.withTarget()
				.function(ctx -> {
					String arg1 = ctx.getOptionValue("arg1");
					return "Hello " + arg1;
				})
				.and()
			.build();

		MissingOptionException moe = new MissingOptionException("msg", registration.getOptions().get(0));
		List<CommandParserException> parserExceptions = Arrays.asList(moe);
		CommandParserExceptionsException cpee = new CommandParserExceptionsException("msg", parserExceptions);
		CommandParserExceptionResolver resolver = new CommandParserExceptionResolver();
		HandlingResult resolve = resolver.resolve(cpee);
		assertThat(resolve).isNotNull();
		assertThat(resolve.message()).contains("--arg1", "Desc arg1");
	}
}
