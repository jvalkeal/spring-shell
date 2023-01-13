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
package org.springframework.shell.samples.e2e;

import org.springframework.context.annotation.Bean;
import org.springframework.shell.command.CommandRegistration;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.stereotype.Component;

/**
 * Commands used for e2e test.
 *
 * @author Janne Valkealahti
 */
public class RequiredValueCommands {

	private final static String REQUIRED_VALUE = "required-value";
	private final static String ARG1_DESC = "Desc arg1";
	private final static String ARG1_NAME = "arg1";

	@ShellComponent
	public static class RequiredValueCommandsLegacyAnnotation extends BaseE2ECommands {

		@ShellMethod(key = LEGACY_ANNO + REQUIRED_VALUE, group = GROUP)
		public String testRequiredValueLegacyAnnotation(
			@ShellOption(help = ARG1_DESC)
			String arg1
		) {
			return "Hello " + arg1;
		}
	}

	@Command(command = BaseE2ECommands.ANNO, group = BaseE2ECommands.GROUP)
	public static class RequiredValueCommandsAnnotation extends BaseE2ECommands {

		@Command(command = REQUIRED_VALUE)
		public String testRequiredValueAnnotation(
			@Option(longNames = ARG1_NAME, required = true, description = ARG1_DESC)
			String arg1
		) {
			return "Hello " + arg1;
		}
	}

	@Component
	public static class RequiredValueCommandsRegistration extends BaseE2ECommands {

		@Bean
		public CommandRegistration testRequiredValueRegistration(CommandRegistration.BuilderSupplier builder) {
			return builder.get()
				.command(REG, REQUIRED_VALUE)
				.group(GROUP)
				.withOption()
					.longNames(ARG1_NAME)
					.description(ARG1_DESC)
					.required()
					.and()
				.withTarget()
					.function(ctx -> {
						String arg1 = ctx.getOptionValue(ARG1_NAME);
						return "Hello " + arg1;
					})
					.and()
				.build();
		}
	}
}
