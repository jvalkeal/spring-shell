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

import org.springframework.shell.component.view.screen.Color;
import org.springframework.shell.component.view.screen.DefaultScreen;
import org.springframework.shell.component.view.screen.Screen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ScreenAssertTests {

	@Test
	void testCursorPosition() {
		Screen screen = new DefaultScreen(5, 5);
		assertThatExceptionOfType(AssertionError.class)
			.isThrownBy(() -> assertThat(forScreen(screen)).hasCursorVisible())
			.withMessageContaining("Expecting a Screen to have a visible cursor");
		assertThatExceptionOfType(AssertionError.class)
			.isThrownBy(() -> assertThat(forScreen(screen)).hasCursorInPosition(1, 1))
			.withMessageContaining("Expecting a Screen to have position <1,1> but was <0,0>");
	}

	@Test
	void hasForegroundColorShouldPass() {
		Screen screen = new DefaultScreen(5, 5);
		screen.print("test", 0, 0, 4, Color.RED, 0);
		assertThat(forScreen(screen)).hasForegroundColor(0, 0, Color.RED);
	}

	@Test
	void hasForegroundColorShouldFail() {
		Screen screen = new DefaultScreen(5, 5);
		screen.print("test", 0, 0, 4, Color.RED, 0);
		assertThatExceptionOfType(AssertionError.class)
			.isThrownBy(() -> assertThat(forScreen(screen)).hasForegroundColor(0, 0, Color.BLUE))
			.withMessageContaining("Expecting a Screen to have foreground color <255> position <0,0> but was <16711680>");
	}

	@Test
	void shouldThrowWithInvalidBounds() {
		Screen screen = new DefaultScreen(5, 5);
		assertThatExceptionOfType(AssertionError.class)
			.isThrownBy(() -> assertThat(forScreen(screen)).hasBorder(0, 0, 0, 0));
	}

	@Test
	void shouldThrowWithInvalidBorder() {
		Screen screen = new DefaultScreen(5, 5);
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
		Screen screen = new DefaultScreen(5, 10);
		screen.printBorder(0, 0, 10, 5);
		assertThat(forScreen(screen)).hasBorder(0, 0, 10, 5);

		screen = new DefaultScreen(10, 5);
		screen.printBorder(0, 0, 5, 10);
		assertThat(forScreen(screen)).hasBorder(0, 0, 5, 10);

		screen = new DefaultScreen(10, 5);
		screen.printBorder(1, 1, 3, 8);
		assertThat(forScreen(screen)).hasBorder(1, 1, 3, 8);
	}

	@Test
	void shouldNotThrowWithValidNonBorder() {
		Screen screen = new DefaultScreen(5, 10);
		screen.printBorder(0, 0, 10, 5);
		assertThat(forScreen(screen)).hasNoBorder(1, 1, 8, 3);
	}

	@Test
	void hasHorizontalText() {
		Screen screen = new DefaultScreen(5, 10);
		screen.print("test", 0, 0, 4);
		assertThat(forScreen(screen)).hasHorizontalText("test", 0, 0, 4);
	}

	@Test
	void hasNoHorizontalText() {
		Screen screen = new DefaultScreen(5, 10);
		screen.print("xxxx", 0, 0, 4);
		assertThat(forScreen(screen)).hasNoHorizontalText("test", 0, 0, 4);
	}

	// @Test
	// void xxx() {
	// 	Screen screen = new Screen(5, 5);
	// 	screen.printBorder(0, 0, 5, 5);
	// 	assertThat(forScreen(screen)).hasBorder(0, 0, 5, 4);
	// }

	private AssertProvider<ScreenAssert> forScreen(Screen screen) {
		return () -> new ScreenAssert(screen);
	}
}
