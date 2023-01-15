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
import org.springframework.shell.standard.ShellComponent;

@ShellComponent
@Command
public class HiddenCommands extends BaseE2ECommands {

	@Command(command = ANNO + "hidden-1", hidden = true)
	public String testHidden1Annotation() {
		return "Hello from hidden command";
	}

	@Bean
	public CommandRegistration testHidden1Registration(CommandRegistration.BuilderSupplier builder) {
		return builder.get()
			.command(REG, "hidden-1")
			.group(GROUP)
			.hidden()
			.withTarget()
				.function(ctx -> {
					return "Hello from hidden command";
				})
				.and()
			.build();
	}
}
