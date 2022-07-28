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
package org.springframework.shell.samples.e2e;

import org.springframework.context.annotation.Bean;
import org.springframework.shell.command.CommandRegistration;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class ShortArgumentCommands extends BaseE2ECommands {

	@ShellMethod(key = LEGACY_ANNO + "short-argument-posix-1", group = GROUP)
	public String testShortPosix1(
		@ShellOption(value = "-a") String argA,
		@ShellOption(value = "-b") String argB
	) {
		return "Hello " + argA + " and " + argB;
	}

	@ShellMethod(key = LEGACY_ANNO + "short-argument-posix-2", group = GROUP)
	public String testShortPosix2(
		@ShellOption(value = "-a") Boolean argA,
		@ShellOption(value = "-b") Boolean argB
	) {
		return "Hello " + argA + " and " + argB;
	}

	@ShellMethod(key = LEGACY_ANNO + "short-argument-long-single", group = GROUP)
	public String testLongSingle(
		@ShellOption(value = { "-arg1"}) String arg1
	) {
		return "Hello " + arg1;
	}

	@Bean
	CommandRegistration testLongSingleRegistration() {
		return CommandRegistration.builder()
			.command(REG, "short-argument-long-single")
			.group(GROUP)
			.withOption()
				.shortNames("arg1")
				.and()
			.withTarget()
				.function(ctx -> {
					String arg1 = ctx.getOptionValue("arg1");
					return "Hello " + arg1;
				})
				.and()
			.build();
	}

}
