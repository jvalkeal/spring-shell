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
package org.springframework.shell.component.xxx;

import java.util.Collections;
import java.util.List;

import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.terminal.Attributes;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.Display;
import org.jline.utils.InfoCmp.Capability;

import org.springframework.util.Assert;

public class ViewApplication {

	public final static String OPERATION_EXIT = "EXIT";

	private final Terminal terminal;
	private final BindingReader bindingReader;
	private final KeyMap<String> keyMap = new KeyMap<>();
	private final VirtualScreen virtualScreen = new VirtualScreen();
	private ViewComponent rootView;

	public ViewApplication(Terminal terminal) {
		Assert.notNull(terminal, "terminal must be set");
		this.terminal = terminal;
		this.bindingReader = new BindingReader(terminal.reader());
	}

	public void setRoot(ViewComponent root, boolean fullScreen) {
		this.rootView = root;
	}

	public void run() {
		bindKeyMap(keyMap);
		loop();
	}

	protected void bindKeyMap(KeyMap<String> keyMap) {
		keyMap.bind(OPERATION_EXIT, "\r");
	}

	protected void render(int rows, int columns) {
		if (rootView == null) {
			return;
		}
		rootView.setRect(0, 0, columns, rows);
		rootView.draw(virtualScreen);
	}

	protected void loop() {
		Display display = new Display(terminal, true);
		Attributes attr = terminal.enterRawMode();
		Size size = new Size();

		try {
			terminal.puts(Capability.keypad_xmit);
			terminal.puts(Capability.cursor_invisible);
			terminal.writer().flush();
			size.copy(terminal.getSize());
			display.clear();
			display.reset();

			while (true) {
				display.resize(size.getRows(), size.getColumns());
				virtualScreen.resize(size.getRows(), size.getColumns());
				// virtualScreen.reset(size.getRows(), size.getColumns());
				render(size.getRows(), size.getColumns());
				List<AttributedString> newLines = virtualScreen.getScreenLines();
				display.update(newLines, 0);
				boolean exit = read(bindingReader, keyMap);
				if (exit) {
					break;
				}
			}
		}
		finally {
			terminal.setAttributes(attr);
			terminal.puts(Capability.keypad_local);
			terminal.puts(Capability.cursor_visible);
			display.update(Collections.emptyList(), 0);
		}
	}

	protected boolean read(BindingReader bindingReader, KeyMap<String> keyMap) {
		String operation = bindingReader.readBinding(keyMap);
		if (operation == null) {
			return true;
		}
		switch (operation) {
			case OPERATION_EXIT:
				return true;
		}

		return false;
	}

}
