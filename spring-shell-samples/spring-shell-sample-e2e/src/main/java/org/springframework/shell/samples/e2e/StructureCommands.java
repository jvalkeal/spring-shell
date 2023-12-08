/*
 * Copyright 2023 the original author or authors.
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

import org.springframework.shell.command.annotation.Command;

public class StructureCommands {

	static final String ANNOC = BaseE2ECommands.ANNO + "structure ";
	static final String ANNOA = BaseE2ECommands.ANNO + "structurealias ";

	@Command(command = ANNOC, alias = ANNOA, group = BaseE2ECommands.GROUP)
	public static class StructureCommandsAnnotation extends BaseE2ECommands {

		@Command()
		public String testParentAnnotation() {
			return "Hello from parent alias command";
		}

		@Command(command = "alias-1", alias = "aliasfor-1")
		public String testAlias1Annotation() {
			return "Hello from alias command";
		}

	}

	@Command(command = "foo", alias = "f")
	public static class MyCommand {
		// @Command(command = "", alias = "")
		@Command
		public String emptySub() {
			return "empty sub";
		}

		@Command(command = "bar", alias = "b")
		public String bar() {
			return "foo bar";
		}
	}
}
