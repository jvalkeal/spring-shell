/*
 * Copyright 2024 the original author or authors.
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

import org.jline.terminal.Terminal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.shell.command.CommandContext;
import org.springframework.shell.command.CommandRegistration;
import org.springframework.shell.command.annotation.Command;
import org.springframework.stereotype.Component;

public class ResultCommands {

	@Command(command = BaseE2ECommands.ANNO, group = BaseE2ECommands.GROUP)
	public static class Annotation extends BaseE2ECommands {

		@Autowired
		Terminal terminal;

		@Command(command = "result")
		public String testStdoutxAnnotation(
			CommandContext commandContext
		) {
			boolean hasPty = commandContext.getShellContext().hasPty();
			return String.format("hasPty='%s'", hasPty);
		}
	}

	// spring:
	//   shell:
	//     render:
	//       machine:
	//         enabled: true
	//         default: json

	@Component
	public static class Registration extends BaseE2ECommands {

		@Bean
		public CommandRegistration testStdoutxRegistration() {
			return getBuilder()
				.command(REG, "result")
				.group(GROUP)
				// .machineType(MachineType.JSON)
				// .machineType(String.class)
				// .machineType(TypeDescriptor type)
				.machineType(TypeDescriptor.valueOf(String.class))
				// .userType(String.class)
				// .userType(TypeDescriptor type)
				// .withOption()
				// 	.type(String.class)
				// 	.and()
				.withTarget()
					.function(ctx -> {
						boolean hasPty = ctx.getShellContext().hasPty();
						return String.format("hasPty='%s'", hasPty);
					})
					.and()
				.build();
		}
	}

}
