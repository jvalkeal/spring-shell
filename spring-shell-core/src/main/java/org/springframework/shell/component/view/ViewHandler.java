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

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.terminal.Attributes;
import org.jline.terminal.MouseEvent;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.Terminal.Signal;
import org.jline.terminal.Terminal.SignalHandler;
import org.jline.utils.AttributedString;
import org.jline.utils.Display;
import org.jline.utils.InfoCmp.Capability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.util.Assert;

import static org.jline.keymap.KeyMap.ctrl;
import static org.jline.keymap.KeyMap.key;

/**
 * Component handling {@link View} structures.
 *
 * @author Janne Valkealahti
 */
public class ViewHandler {

	private final static Logger log = LoggerFactory.getLogger(ViewHandler.class);
	public final static String OPERATION_EXIT = "EXIT";
	public final static String OPERATION_REPAINT = "REPAINT";
	public final static String OPERATION_MOUSE_EVENT = "MOUSE_EVENT";

	private final Terminal terminal;
	private final BindingReader bindingReader;
	private final KeyMap<String> keyMap = new KeyMap<>();
	private final Screen virtualDisplay = new Screen();
	private Display display;
	private Size size;
	private View rootView;

	public ViewHandler(Terminal terminal) {
		Assert.notNull(terminal, "terminal must be set");
		this.terminal = terminal;
		this.bindingReader = new BindingReader(terminal.reader());
	}

	public void setRoot(View root, boolean fullScreen) {
		this.rootView = root;
		this.rootView.focus();
	}

	public void redraw() {
		display();
	}

	public void run() {
		bindKeyMap(keyMap);
		display = new Display(terminal, true);
		size = new Size();
		loop();
	}

	protected void bindKeyMap(KeyMap<String> keyMap) {
		keyMap.bind(OPERATION_EXIT, "\r");
		// keyMap.bind(OPERATION_REPAINT, "r", ctrl('R'), ctrl('L'));
		keyMap.bind(OPERATION_MOUSE_EVENT, key(terminal, Capability.key_mouse));
	}

	protected void render(int rows, int columns) {
		if (rootView == null) {
			return;
		}
		rootView.setRect(0, 0, columns, rows);
		rootView.draw(virtualDisplay);
	}

	private void display() {
		log.debug("display");
		size.copy(terminal.getSize());
		display.resize(size.getRows(), size.getColumns());
		display.clear();
		display.reset();
		rootView.setRect(0, 0, size.getColumns(), size.getRows());
		virtualDisplay.resize(size.getRows(), size.getColumns());
		render(size.getRows(), size.getColumns());
		// List<AttributedString> newLines = virtualDisplay.getScreenLines();
		List<AttributedString> newLines = virtualDisplay.getScreenLines();
		display.update(newLines, 0);
	}

	protected void loop() {
		Attributes attr = terminal.enterRawMode();

		terminal.handle(Signal.WINCH, signal -> {
			log.debug("Handling signal {}", signal);
			display();
		});

		try {
			terminal.puts(Capability.keypad_xmit);
			terminal.puts(Capability.cursor_invisible);
			terminal.trackMouse(Terminal.MouseTracking.Normal);
			terminal.writer().flush();
			size.copy(terminal.getSize());
			display.clear();
			display.reset();

			while (true) {
				display();
				boolean exit = read(bindingReader, keyMap);
				if (exit) {
					break;
				}
			}
		}
		finally {
			terminal.setAttributes(attr);
			terminal.trackMouse(Terminal.MouseTracking.Off);
			terminal.puts(Capability.keypad_local);
			terminal.puts(Capability.cursor_visible);
			display.update(Collections.emptyList(), 0);
		}
	}

	protected boolean read(BindingReader bindingReader, KeyMap<String> keyMap) {
		String operation = bindingReader.readBinding(keyMap);
		// String operation = bindingReader.readBinding(keyMap, null, false);
		log.debug("Read got operation {}", operation);
		if (operation == null) {
			return true;
		}
		if (rootView != null) {
			Consumer<String> inputConsumer = rootView.getInputConsumer();
			if (inputConsumer != null) {
				inputConsumer.accept(operation);
			}
		}
		switch (operation) {
			case OPERATION_EXIT:
				return true;
			case OPERATION_MOUSE_EVENT:
				mouseEvent();
				break;
			// case OPERATION_REPAINT:
			// 	// size.copy(terminal.getSize());
			// 	// display.clear();
			// 	break;

		}

		return false;
	}

    void mouseEvent() {
        MouseEvent event = terminal.readMouseEvent();
		log.info("MOUSE: {}", event);
        // if (event.getModifiers().isEmpty() && event.getType() == MouseEvent.Type.Released
        //         && event.getButton() == MouseEvent.Button.Button1) {
        //     int x = event.getX();
        //     int y = event.getY();
        // }
        // else if (event.getType() == MouseEvent.Type.Wheel) {
        //     if (event.getButton() == MouseEvent.Button.WheelDown) {
        //     } else if (event.getButton() == MouseEvent.Button.WheelUp) {
        //     }
        // }
    }

}