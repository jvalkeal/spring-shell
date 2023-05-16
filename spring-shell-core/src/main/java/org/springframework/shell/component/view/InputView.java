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

import java.util.function.BiFunction;
import java.util.function.Consumer;

public class InputView extends BoxView {

	private final StringBuilder text = new StringBuilder();
	private int cursorPosition = 0;

	@Override
	public BiFunction<KeyEvent, Consumer<View>, KeyEvent> getInputHandler() {
		return (event, view) -> {
			if (event.key() == null) {
				String data = event.data();
				add(data);
			}
			else {
				switch (event.key()) {
					case BACKSPACE:
						backspace();
						break;
					case DELETE:
						delete();
						break;
					case LEFT:
						left();
						break;
					case RIGHT:
						right();
						break;
					default:
						break;
				}
			}
			return event;
		};
	}

	@Override
	protected void drawInternal(Screen screen) {
		Rectangle rect = getInnerRect();
		String s = text.toString();
		screen.print(s, rect.x(), rect.y(), s.length());
		screen.setShowCursor(hasFocus());
		screen.setCursorPosition(new Position(rect.x() + cursorPosition, rect.y()));
		super.drawInternal(screen);
	}

	private void add(String data) {
		text.append(data);
		right();
	}

	private void backspace() {
		if (cursorPosition > 0) {
			text.deleteCharAt(cursorPosition - 1);
		}
		left();
	}

	private void delete() {
		text.deleteCharAt(cursorPosition);
	}

	private void left() {
		cursorPosition--;
	}

	private void right() {
		cursorPosition++;
	}
}
