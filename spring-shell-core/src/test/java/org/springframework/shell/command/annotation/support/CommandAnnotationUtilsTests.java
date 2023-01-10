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
package org.springframework.shell.command.annotation.support;

import org.junit.jupiter.api.Test;

import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.shell.command.annotation.Command;

import static org.assertj.core.api.Assertions.assertThat;

class CommandAnnotationUtilsTests {

	private static MergedAnnotation<Command> hiddenTrue = MergedAnnotations.from(HiddenTrue.class).get(Command.class);
	private static MergedAnnotation<Command> hiddenFalse = MergedAnnotations.from(HiddenFalse.class).get(Command.class);
	private static MergedAnnotation<Command> hiddenDefault = MergedAnnotations.from(HiddenDefault.class).get(Command.class);

	@Test
	void testHidden() {
		assertThat(CommandAnnotationUtils.deduceHidden(hiddenTrue, hiddenDefault)).isTrue();
		assertThat(CommandAnnotationUtils.deduceHidden(hiddenTrue, hiddenFalse)).isTrue();
		assertThat(CommandAnnotationUtils.deduceHidden(hiddenTrue, hiddenTrue)).isTrue();

		assertThat(CommandAnnotationUtils.deduceHidden(hiddenFalse, hiddenDefault)).isFalse();
		assertThat(CommandAnnotationUtils.deduceHidden(hiddenFalse, hiddenFalse)).isFalse();
		assertThat(CommandAnnotationUtils.deduceHidden(hiddenFalse, hiddenTrue)).isTrue();
	}

	@Command(hidden = true)
	private static class HiddenTrue {
	}

	@Command(hidden = false)
	private static class HiddenFalse {
	}

	@Command()
	private static class HiddenDefault {
	}

	@Test
	void testCommand() {
		assertThat(CommandAnnotationUtils.deduceCommand(commandDefault, commandDefault)).isEmpty();
		assertThat(CommandAnnotationUtils.deduceCommand(commandDefault, commandValues1))
				.isEqualTo(new String[] { "one", "two" });
		assertThat(CommandAnnotationUtils.deduceCommand(commandValues1, commandValues2))
				.isEqualTo(new String[] { "one", "two", "three", "four" });
		assertThat(CommandAnnotationUtils.deduceCommand(commandDefault, commandValues3))
				.isEqualTo(new String[] { "five", "six", "seven" });
		assertThat(CommandAnnotationUtils.deduceCommand(commandDefault, commandValues4))
				.isEqualTo(new String[] { "eight", "nine" });
	}

	private static MergedAnnotation<Command> commandDefault = MergedAnnotations.from(CommandDefault.class).get(Command.class);
	private static MergedAnnotation<Command> commandValues1 = MergedAnnotations.from(CommandValues1.class).get(Command.class);
	private static MergedAnnotation<Command> commandValues2 = MergedAnnotations.from(CommandValues2.class).get(Command.class);
	private static MergedAnnotation<Command> commandValues3 = MergedAnnotations.from(CommandValues3.class).get(Command.class);
	private static MergedAnnotation<Command> commandValues4 = MergedAnnotations.from(CommandValues4.class).get(Command.class);

	@Command
	private static class CommandDefault {
	}

	@Command(command = { "one", "two" })
	private static class CommandValues1 {
	}

	@Command(command = { "three", "four" })
	private static class CommandValues2 {
	}

	@Command(command = { " five", "six ", " seven " })
	private static class CommandValues3 {
	}

	@Command(command = { " eight nine " })
	private static class CommandValues4 {
	}

}
