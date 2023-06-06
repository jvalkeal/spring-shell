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
import org.springframework.shell.component.view.View;
import org.springframework.shell.component.view.event.DefaultEventLoop;
import org.springframework.shell.component.view.event.EventLoop;
import org.springframework.shell.component.view.event.KeyBinder;
import org.springframework.shell.component.view.event.KeyEvent;
import org.springframework.shell.component.view.event.KeyEvent.ModType;
import org.springframework.shell.component.view.event.KeyHandler;
import org.springframework.shell.component.view.event.KeyHandler.KeyHandlerResult;
import org.springframework.shell.component.view.event.MouseHandler;
import org.springframework.shell.component.view.event.MouseHandler.MouseHandlerResult;
import org.springframework.shell.component.view.geom.Rectangle;
import org.springframework.shell.component.view.message.ShellMessageBuilder;
import org.springframework.shell.component.view.message.ShellMessageHeaderAccessor;
import org.springframework.shell.component.view.message.StaticShellMessageHeaderAccessor;
import org.springframework.shell.component.view.screen.DefaultScreen;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * Component handling {@link View} structures.
 *
 * @author Janne Valkealahti
 */
public class TerminalUI {

	private final static Logger log = LoggerFactory.getLogger(TerminalUI.class);
	private final Terminal terminal;
	private final BindingReader bindingReader;
	private final KeyMap<String> keyMap = new KeyMap<>();
	private final DefaultScreen virtualDisplay = new DefaultScreen();
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
		log.trace("display()");
		size.copy(terminal.getSize());
		if (fullScreen) {
			display.clear();
			display.resize(size.getRows(), size.getColumns());
			display.reset();
			rootView.setRect(0, 0, size.getColumns(), size.getRows());
			virtualDisplay.resize(size.getRows(), size.getColumns());
			render(size.getRows(), size.getColumns());
		}
		else {
			display.resize(size.getRows(), size.getColumns());
			Rectangle rect = rootView.getRect();
			virtualDisplay.resize(rect.height(), rect.width());
			render(rect.height(), rect.width());
		}

		List<AttributedString> newLines = virtualDisplay.getScreenLines();
		int targetCursorPos = 0;
		if (virtualDisplay.isShowCursor()) {
			terminal.puts(Capability.cursor_visible);
			targetCursorPos = size.cursorPos(virtualDisplay.getCursorPosition().y(), virtualDisplay.getCursorPosition().x());
			log.debug("Display targetCursorPos {}", targetCursorPos);
		}
		display.update(newLines, targetCursorPos);
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
		if (rootView != null && rootView.hasFocus()) {
			// BiConsumer<KeyEvent, Consumer<View>> inputHandler = rootView.getInputHandler();
			// if (inputHandler != null) {
			// 	inputHandler.accept(event, this::setFocus);
			// }

			KeyHandler handler = rootView.getKeyHandler();
			if (handler != null) {
				KeyHandlerResult result = handler.handle(KeyHandler.argsOf(event));
				log.debug(("handleKeyEvent {}"), result);
				if (result.focus() != null) {
					setFocus(result.focus());
				}
			}

		}
	}

	private void handleMouseEvent(MouseEvent event) {
		if (rootView != null) {
			MouseHandler handler = rootView.getMouseHandler();
			if (handler != null) {
				MouseHandlerResult result = handler.handle(MouseHandler.argsOf(event));
				log.debug(("handleMouseEvent {}"), result);
				if (result.focus() != null) {
					setFocus(result.focus());
				}
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

		try {
			if (fullScreen) {
				terminal.puts(Capability.enter_ca_mode);
			}
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
			terminal.setAttributes(attr);
			log.debug("Setting cursor visible");
			terminal.puts(Capability.cursor_visible);
			if (fullScreen) {
				display.update(Collections.emptyList(), 0);
			}
			terminal.trackMouse(Terminal.MouseTracking.Off);
			if (fullScreen) {
				terminal.puts(Capability.exit_ca_mode);
			}
			terminal.puts(Capability.keypad_local);
			if (!fullScreen) {
				display.update(Collections.emptyList(), 0);
			}
		}
	}

	private void bindKeyMap(KeyMap<String> keyMap) {
		keyBinder.bindAll(keyMap);
	}

	private boolean read(BindingReader bindingReader, KeyMap<String> keyMap) {
        Thread readThread = Thread.currentThread();
		terminal.handle(Signal.INT, signal -> {
			log.debug("Handling signal {}", signal);
			readThread.interrupt();
		});

		String operation = null;
		try {
			operation = bindingReader.readBinding(keyMap);
			log.debug("Read got operation {}", operation);
        } catch (IOError e) {
            // Ignore Ctrl+C interrupts and just exit the loop
			log.trace("Read binding error {}", e);
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
		else if (operation.equals("OPERATION_CHAR")) {
			String lastBinding = bindingReader.getLastBinding();
			dispatchChar(lastBinding, false, false);
		}
		else if (operation.equals("OPERATION_MOUSE")) {
			mouseEvent();
		}

		return false;
	}

	private void dispatchChar(String binding, boolean ctrl, boolean alt) {
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
