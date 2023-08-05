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
package org.springframework.shell.component.view.control;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.shell.component.view.event.KeyEvent;
import org.springframework.shell.component.view.event.KeyEvent.Key;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class InputViewTests extends AbstractViewTests {

	private static final String CURSOR_POSITION_FIELD = "cursorPosition";

	@Nested
	class Input {

		InputView view;

		@BeforeEach
		void setup() {
			view = new InputView();
			configure(view);
		}

		@Test
		void shouldShowPlainText() {
			view.setShowBorder(true);
			view.setRect(0, 0, 80, 24);

			dispatchEvent(view, KeyEvent.of('1'));
			view.draw(screen24x80);

			assertThat(forScreen(screen24x80)).hasHorizontalText("1", 1, 1, 1);
			assertThat(forScreen(screen24x80)).hasCursorInPosition(2, 1);
		}

		@Test
		void shouldShowUnicode() {
			view.setShowBorder(true);
			view.setRect(0, 0, 80, 24);

			dispatchEvent(view, KeyEvent.of('★'));
			view.draw(screen24x80);

			assertThat(forScreen(screen24x80)).hasHorizontalText("★", 1, 1, 1);
			assertThat(forScreen(screen24x80)).hasCursorInPosition(2, 1);
		}

		@Test
		void shouldShowUnicodeEmoji() {
			view.setShowBorder(true);
			view.setRect(0, 0, 80, 24);

			dispatchEvent(view, KeyEvent.of("😂"));
			view.draw(screen24x80);

			assertThat(forScreen(screen24x80)).hasHorizontalText("😂", 1, 1, 2);
			assertThat(forScreen(screen24x80)).hasCursorInPosition(3, 1);
		}

	}

	@Nested
	class CursorPositions {

		InputView view;

		@BeforeEach
		void setup() {
			view = new InputView();
			configure(view);
		}

		int cursorPosition() {
			return (Integer) ReflectionTestUtils.getField(view, CURSOR_POSITION_FIELD);
		}

		@Test
		void initialCursorPosition() {
			assertThat(cursorPosition()).isEqualTo(0);
		}

		@Test
		void shouldNotMoveOutOfBoundsIfMovingRight() {
			handleKey(view, Key.CursorRight);
			assertThat(cursorPosition()).isEqualTo(0);
		}

		@Test
		void shouldNotMoveOutOfBoundsIfMovingLeft() {
			handleKey(view, Key.CursorLeft);
			assertThat(cursorPosition()).isEqualTo(0);
		}

		@Test
		void shouldMoveWithInputKeysNarrow() {
			handleKey(view, Key.a);
			assertThat(cursorPosition()).isEqualTo(1);
		}

		@Test
		void shouldMoveWithInputKeysWide() {
			handleKey(view, "😂");
			assertThat(cursorPosition()).isEqualTo(2);
		}

	}

	@Nested
	class MoveAndDeletions {

		InputView view;

		@BeforeEach
		void setup() {
			view = new InputView();
			configure(view);
		}

		int cursorPosition() {
			return (Integer) ReflectionTestUtils.getField(view, CURSOR_POSITION_FIELD);
		}

		@Test
		void addEmojiAndBackspace() {
			handleKey(view, "😂");
			assertThat(cursorPosition()).isEqualTo(2);
			handleKey(view, Key.Backspace);
			assertThat(cursorPosition()).isEqualTo(0);
		}

	}


}
