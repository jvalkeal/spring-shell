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
package org.springframework.shell.component;

import java.io.IOError;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.terminal.Attributes;
import org.jline.terminal.MouseEvent;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.Terminal.Signal;
import org.jline.utils.AttributedString;
import org.jline.utils.Display;
import org.jline.utils.InfoCmp.Capability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.shell.component.view.KeyBinder;
import org.springframework.shell.component.view.KeyEvent;
import org.springframework.shell.component.view.KeyEvent.ModType;
import org.springframework.shell.component.view.Screen;
import org.springframework.shell.component.view.View;
import org.springframework.shell.component.view.eventloop.DefaultEventLoop;
import org.springframework.shell.component.view.eventloop.EventLoop;
import org.springframework.shell.component.view.message.ShellMessageBuilder;
import org.springframework.shell.component.view.message.ShellMessageHeaderAccessor;
import org.springframework.shell.component.view.message.StaticShellMessageHeaderAccessor;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import static org.jline.keymap.KeyMap.key;

/**
 * Component handling {@link View} structures.
 *
 * @author Janne Valkealahti
 */
public class TerminalUI {

	private final static Logger log = LoggerFactory.getLogger(TerminalUI.class);
	// public final static String OPERATION_EXIT = "EXIT";
	public final static String OPERATION_REDRAW = "REDRAW";
	public final static String OPERATION_MOUSE_EVENT = "MOUSE_EVENT";
	public final static String OPERATION_KEY_EVENT = "CHAR";

	private final Terminal terminal;
	private final BindingReader bindingReader;
	private final KeyMap<String> keyMap = new KeyMap<>();
	private final Screen virtualDisplay = new Screen();
	private Display display;
	private Size size;
	private View rootView;
	private boolean fullScreen;
	private final KeyBinder keyBinder;
	private DefaultEventLoop eventLoop = new DefaultEventLoop();

	/**
	 * Constructs a handler with a given terminal.
	 *
	 * @param terminal the terminal
	 */
	public TerminalUI(Terminal terminal) {
		Assert.notNull(terminal, "terminal must be set");
		this.terminal = terminal;
		this.bindingReader = new BindingReader(terminal.reader());
		this.keyBinder = new KeyBinder(terminal);
	}

	/**
	 * Sets a root view of this handler.
	 *
	 * @param root the root view
	 * @param fullScreen if root view should request full screen
	 */
	public void setRoot(View root, boolean fullScreen) {
		setFocus(root);
		this.rootView = root;
		this.fullScreen = fullScreen;
		this.rootView.getMessageListeners().register(e -> {
			View view = StaticShellMessageHeaderAccessor.getView(e);
			if (view != null) {
				if (e.getPayload() instanceof String s) {
					if ("enter".equals(s)) {
						this.terminal.raise(Signal.INT);
					}
				}
			}
		});
	}

	private View focus = null;
	private void setFocus(@Nullable View view) {
		if (focus != null) {
			focus.focus(focus, false);
		}
		focus = view;
		if (focus != null) {
			focus.focus(focus, true);
		}
	}

	/**
	 * Run and start execution loop. This method blocks until run loop exits.
	 */
	public void run() {
		bindKeyMap(keyMap);
		display = new Display(terminal, fullScreen);
		size = new Size();
		loop();
	}

	public EventLoop getEventLoop() {
		return eventLoop;
	}

	public void redraw() {
		getEventLoop().dispatch(ShellMessageBuilder.ofRedraw());
	}

	private void render(int rows, int columns) {
		if (rootView == null) {
			return;
		}
		rootView.setRect(0, 0, columns, rows);
		rootView.draw(virtualDisplay);
	}

	private void display() {
		log.debug("display");
		size.copy(terminal.getSize());
		display.clear();
		display.resize(size.getRows(), size.getColumns());
		display.reset();
		if (fullScreen) {
			rootView.setRect(0, 0, size.getColumns(), size.getRows());
			virtualDisplay.resize(size.getRows(), size.getColumns());
			render(size.getRows(), size.getColumns());
		}
		else {
			rootView.setRect(0, 0, 10, 7);
			virtualDisplay.resize(7, 10);
			render(7, 10);
		}
		// rootView.setRect(0, 0, size.getColumns(), size.getRows());
		// virtualDisplay.resize(size.getRows(), size.getColumns());
		// testing not full screen
		// rootView.setRect(0, 0, size.getColumns(), 5);
		// virtualDisplay.resize(5, size.getColumns());
		// render(size.getRows(), size.getColumns());
		List<AttributedString> newLines = virtualDisplay.getScreenLines();

		int xxx = 0;
		if (virtualDisplay.isShowCursor()) {
			terminal.puts(Capability.cursor_visible);
			xxx = size.cursorPos(virtualDisplay.getCursorPosition().y(), virtualDisplay.getCursorPosition().x());
			log.debug("XXX cursor pos {}", xxx);
		}

		display.update(newLines, xxx);
	}

	private void dispatchWinch() {
		Message<String> message = MessageBuilder.withPayload("WINCH")
			.setHeader(ShellMessageHeaderAccessor.EVENT_TYPE, EventLoop.Type.SIGNAL)
			.build();
		eventLoop.dispatch(message);
	}

	private void registerEventHandling() {
		eventLoop.events()
			.filter(m -> {
				return ObjectUtils.nullSafeEquals(m.getHeaders().get(ShellMessageHeaderAccessor.EVENT_TYPE), EventLoop.Type.SIGNAL);
			})
			.doOnNext(m -> {
				display();
			})
			.subscribe();

		eventLoop.events()
			.filter(m -> {
				return ObjectUtils.nullSafeEquals(m.getHeaders().get(ShellMessageHeaderAccessor.EVENT_TYPE), EventLoop.Type.SYSTEM);
			})
			.doOnNext(m -> {
				Object payload = m.getPayload();
				if (payload instanceof String s) {
					if ("redraw".equals(s)) {
						display();
					}
				}
			})
			.subscribe();

		eventLoop.keyEvents()
			.doOnNext(m -> {
				handleKeyEvent(m);
			})
			.subscribe();

		eventLoop.events()
			.filter(m -> {
				return ObjectUtils.nullSafeEquals(m.getHeaders().get(ShellMessageHeaderAccessor.EVENT_TYPE), EventLoop.Type.MOUSE);
			})
			.doOnNext(m -> {
				Object payload = m.getPayload();
				if (payload instanceof MouseEvent p) {
					handleMouseEvent(p);
				}
			})
			.subscribe();
	}

	private void handleKeyEvent(KeyEvent event) {
		if (rootView != null) {
			BiConsumer<KeyEvent, Consumer<View>> inputHandler = rootView.getInputHandler();
			if (inputHandler != null) {
				inputHandler.accept(event, this::setFocus);
			}
		}
	}

	private void handleMouseEvent(MouseEvent event) {
		if (rootView != null) {
			BiFunction<MouseEvent, Consumer<View>, MouseEvent> mouseHandler = rootView.getMouseHandler();
			if (mouseHandler != null) {
				Consumer<View> asdf = v -> {
					setFocus(v);
				};
				mouseHandler.apply(event, asdf);
			}
		}
	}

	private void loop() {
		Attributes attr = terminal.enterRawMode();
		registerEventHandling();

		terminal.handle(Signal.WINCH, signal -> {
			log.debug("Handling signal {}", signal);
			dispatchWinch();
		});
		// terminal.handle(Signal.INT, signal -> {
		// 	log.debug("Handling signal {}", signal);
		// });

		try {
			terminal.puts(Capability.enter_ca_mode);
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
			eventLoop.destroy();
			display.update(Collections.emptyList(), 0);
			terminal.trackMouse(Terminal.MouseTracking.Off);
			terminal.puts(Capability.exit_ca_mode);
			terminal.puts(Capability.clear_screen);
			terminal.puts(Capability.keypad_local);
			terminal.puts(Capability.cursor_visible);
			terminal.setAttributes(attr);
			// display.update(Collections.emptyList(), 0);s
		}
	}

	private void bindKeyMap(KeyMap<String> keyMap) {
		keyBinder.bindAll(keyMap);
		keyMap.bind(OPERATION_MOUSE_EVENT, key(terminal, Capability.key_mouse));

		// skip 127 - DEL
		for (char i = 32; i < KeyMap.KEYMAP_LENGTH - 1; i++) {
			keyMap.bind(OPERATION_KEY_EVENT, Character.toString(i));
		}
	}

	private boolean read(BindingReader bindingReader, KeyMap<String> keyMap) {
        Thread readThread = Thread.currentThread();
		terminal.handle(Signal.INT, signal -> readThread.interrupt());

		// String operation = bindingReader.readBinding(keyMap);
		// log.debug("Read got operation {}", operation);
		String operation = null;
		try {
			operation = bindingReader.readBinding(keyMap);
			log.debug("Read got operation {}", operation);
        } catch (IOError e) {
            // Ignore Ctrl+C interrupts and just exit the loop
            // if (!(e.getCause() instanceof InterruptedException)) {
            //     throw e;
            // }
			log.debug("Read binding error {}", e);
		}
		if (operation == null) {
			return true;
		}
		if (operation.startsWith("OPERATION_KEY_")) {
			String exp = operation.substring(14);
			KeyEvent keyEvent = keyBinder.parseKeyEvent(exp);
			dispatchKeyEvent(keyEvent);
			return false;
		}
		switch (operation) {
			case OPERATION_MOUSE_EVENT:
				mouseEvent();
				break;
			case OPERATION_KEY_EVENT:
				String lastBinding = bindingReader.getLastBinding();
				dispatchChar(lastBinding, false, false);
				break;
		}

		return false;
	}

	private void dispatchChar(String binding, boolean ctrl, boolean alt) {
		log.trace("Dispatching {} with {}", OPERATION_KEY_EVENT, binding);
		KeyEvent event = KeyEvent.ofCharacter(binding, ModType.of(ctrl, alt, false));
		Message<KeyEvent> message = MessageBuilder
			.withPayload(event)
			.setHeader(ShellMessageHeaderAccessor.EVENT_TYPE, EventLoop.Type.KEY)
			.build();
		eventLoop.dispatch(message);
	}

	private void dispatchKeyEvent(KeyEvent event) {
		Message<KeyEvent> message = MessageBuilder
			.withPayload(event)
			.setHeader(ShellMessageHeaderAccessor.EVENT_TYPE, EventLoop.Type.KEY)
			.build();
		eventLoop.dispatch(message);
	}

	private void dispatchMouse(MouseEvent event) {
		log.debug("Dispatch mouse event: {}", event);
		eventLoop.dispatch(ShellMessageBuilder.ofMouseEvent(event));
	}

    private void mouseEvent() {
        MouseEvent event = terminal.readMouseEvent();
		dispatchMouse(event);
    }
}
