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

import java.util.EnumSet;

import org.jline.terminal.MouseEvent;

import org.springframework.shell.component.view.event.KeyEvent;
import org.springframework.shell.component.view.event.KeyEvent.Key;
import org.springframework.shell.component.view.event.KeyHandler;
import org.springframework.shell.component.view.geom.Position;
import org.springframework.shell.component.view.geom.Rectangle;
import org.springframework.shell.component.view.screen.Screen;

/**
 * {@code InputView} is used as a text input.
 *
 * @author Janne Valkealahti
 */
public class InputView extends BoxView {

	private final StringBuilder text = new StringBuilder();
	private int cursorPosition = 0;

	@Override
	protected void initInternal() {
		// registerRunnableCommand(ViewCommand.RIGHT, () -> right());
		// registerKeyBinding(Key.CursorRight, ViewCommand.RIGHT);



		// registerKeyCommandXxx1(ViewCommand.RIGHT, () -> right());

		// registerKeyBinding1(Key.CursorRight, ViewCommand.RIGHT);
		registerKeyBinding2(Key.CursorRight, event -> right());
		// registerKeyBinding3(Key.CursorRight, () -> right());
		// registerKeyBinding2(Key.Delete, event -> delete());
		registerKeyBinding3(Key.Backspace, () -> backspace());

	}

	@Override
	public KeyHandler getKeyHandler() {
		KeyHandler handler = args -> {
			KeyEvent event = args.event();
			// if (event.isKey(Key.Backspace)) {
			// 	backspace();
			// }
			// if (event.isKey(Key.Delete)) {
			// 	delete();
			// }
			// else if (event.isKey(Key.CursorLeft)) {
			// 	left();
			// }
			// else if (event.isKey(Key.CursorRight)) {
			// 	right();
			// }
			// else if (event.isKey(Key.Enter)) {
			// 	enter(event);
			// }
			// else if (event.isKey(Key.Tab)) {
			// 	leave(event);
			// }
			// else {
				int plainKey = event.getPlainKey();
				add(new String(new char[]{(char)plainKey}));
			// }

			return KeyHandler.resultOf(event, true, null);
		};
		return handler.fromIfConsumed(super.getKeyHandler());
		// return handler;
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

	public String getInputText() {
		return text.toString();
	}

	private void enter(KeyEvent event) {
		// getShellMessageListener().onMessage(ShellMessageBuilder.ofViewFocus("enter", this));
	}

	private void leave(KeyEvent event) {
		// getShellMessageListener().onMessage(ShellMessageBuilder.ofViewFocus("leave", this));
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
