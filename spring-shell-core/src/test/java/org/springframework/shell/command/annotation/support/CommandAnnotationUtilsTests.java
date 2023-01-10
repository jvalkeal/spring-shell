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
		assertThat(CommandAnnotationUtils.resolveBoolean("hidden", hiddenTrue, hiddenDefault)).isTrue();
		assertThat(CommandAnnotationUtils.resolveBoolean("hidden", hiddenTrue, hiddenFalse)).isTrue();
		assertThat(CommandAnnotationUtils.resolveBoolean("hidden", hiddenTrue, hiddenTrue)).isTrue();

		assertThat(CommandAnnotationUtils.resolveBoolean("hidden", hiddenFalse, hiddenDefault)).isFalse();
		assertThat(CommandAnnotationUtils.resolveBoolean("hidden", hiddenFalse, hiddenFalse)).isFalse();
		assertThat(CommandAnnotationUtils.resolveBoolean("hidden", hiddenFalse, hiddenTrue)).isTrue();
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

}
