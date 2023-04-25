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
package org.springframework.shell.component.view;

import org.assertj.core.api.AssertProvider;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ScreenAssertTests {

	@Test
	void shouldThrowWithInvalidBounds() {
		Screen screen = new Screen(5, 5);
		assertThatExceptionOfType(AssertionError.class)
			.isThrownBy(() -> assertThat(forScreen(screen)).hasBorder(0, 0, 0, 0));
	}

	@Test
	void shouldThrowWithInvalidBorder() {
		Screen screen = new Screen(5, 5);
		screen.printBorder(0, 0, 5, 5);
		assertThatExceptionOfType(AssertionError.class)
			.isThrownBy(() -> assertThat(forScreen(screen)).hasBorder(0, 0, 5, 4));
		assertThatExceptionOfType(AssertionError.class)
			.isThrownBy(() -> assertThat(forScreen(screen)).hasBorder(0, 0, 5, 4));
		assertThatExceptionOfType(AssertionError.class)
			.isThrownBy(() -> assertThat(forScreen(screen)).hasBorder(0, 0, 4, 4));
		assertThatExceptionOfType(AssertionError.class)
			.isThrownBy(() -> assertThat(forScreen(screen)).hasBorder(1, 1, 3, 3));
	}

	@Test
	void shouldNotThrowWithValidBorder() {
		Screen screen = new Screen(5, 5);
		screen.printBorder(0, 0, 5, 5);
		assertThat(forScreen(screen)).hasBorder(0, 0, 5, 5);
	}

	private AssertProvider<ScreenAssert> forScreen(Screen screen) {
		return () -> new ScreenAssert(screen);
	}
}