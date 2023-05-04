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

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

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
import reactor.core.Disposable;

import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import static org.jline.keymap.KeyMap.key;

/**
 * Component handling {@link View} structures.
 *
 * @author Janne Valkealahti
 */
public class ViewHandler {

	private final static Logger log = LoggerFactory.getLogger(ViewHandler.class);
	public final static String OPERATION_EXIT = "EXIT";
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

	private EventLoop eventLoop = new EventLoop();

	/**
	 * Constructs a handler with a given terminal.
	 *
	 * @param terminal the terminal
	 */
	public ViewHandler(Terminal terminal) {
		Assert.notNull(terminal, "terminal must be set");
		this.terminal = terminal;
		this.bindingReader = new BindingReader(terminal.reader());
	}

	/**
	 * Sets a root view of this handler.
	 *
	 * @param root the root view
	 * @param fullScreen if root view should request full screen
	 */
	public void setRoot(View root, boolean fullScreen) {
		// this.rootView.focus(root, true);
		setFocus(root);
		this.rootView = root;
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
		display = new Display(terminal, true);
		size = new Size();
		loop();
	}

	public EventLoop getEventLoop() {
		return eventLoop;
	}

	private void redraw() {
		display();
	}

	private void bindKeyMap(KeyMap<String> keyMap) {
		keyMap.bind(OPERATION_EXIT, "\r");
		keyMap.bind(OPERATION_MOUSE_EVENT, key(terminal, Capability.key_mouse));

		// skip 127 - DEL
		for (char i = 32; i < KeyMap.KEYMAP_LENGTH - 1; i++) {
			keyMap.bind(OPERATION_KEY_EVENT, Character.toString(i));
		}
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
		display.resize(size.getRows(), size.getColumns());
		display.clear();
		display.reset();
		rootView.setRect(0, 0, size.getColumns(), size.getRows());
		virtualDisplay.resize(size.getRows(), size.getColumns());
		render(size.getRows(), size.getColumns());
		List<AttributedString> newLines = virtualDisplay.getScreenLines();
		display.update(newLines, 0);
	}

	private void dispatchWinch() {
		Message<String> message = MessageBuilder.withPayload("WINCH")
			.setHeader(EventLoop.TYPE, EventLoop.Type.SIGNAL)
			.build();
		eventLoop.dispatch(message);
	}

	private void registerEventHandling() {
		Disposable subscribe1 = eventLoop.events()
			.filter(m -> {
				return ObjectUtils.nullSafeEquals(m.getHeaders().get(EventLoop.TYPE), EventLoop.Type.SIGNAL);
			})
			.doOnNext(m -> {
				display();
			})
			.subscribe();

		Disposable subscribe2 = eventLoop.events()
			.filter(m -> {
				return ObjectUtils.nullSafeEquals(m.getHeaders().get(EventLoop.TYPE), EventLoop.Type.KEY);
			})
			.doOnNext(m -> {
				Object payload = m.getPayload();
				if (payload instanceof KeyEvent p) {
					xxx(p);
				}
			})
			.subscribe();

		Disposable subscribe3 = eventLoop.events()
			.filter(m -> {
				return ObjectUtils.nullSafeEquals(m.getHeaders().get(EventLoop.TYPE), EventLoop.Type.MOUSE);
			})
			.doOnNext(m -> {
				Object payload = m.getPayload();
				if (payload instanceof MouseEvent p) {
					xxxm(p);
				}
			})
			.subscribe();
	}

	private void xxx(KeyEvent binding) {
		if (rootView != null) {
			BiFunction<KeyEvent, Consumer<View>, KeyEvent> inputHandler = rootView.getInputHandler();
			if (inputHandler != null) {
				Consumer<View> asdf = v -> {
					setFocus(v);
				};
				KeyEvent apply = inputHandler.apply(binding, asdf);
			}
			// Consumer<String> inputConsumer = rootView.getInputConsumer();
			// if (inputConsumer != null) {
			// 	inputConsumer.accept(binding);
			// }
		}
	}

	private void xxxm(MouseEvent e) {
		if (rootView != null) {
			// Function<MouseEvent, MouseEvent> mouseHandler = rootView.getMouseHandler();
			BiFunction<MouseEvent, Consumer<View>, MouseEvent> mouseHandler = rootView.getMouseHandler();
			if (mouseHandler != null) {
				Consumer<View> asdf = v -> {
					setFocus(v);
				};
				MouseEvent apply = mouseHandler.apply(e, asdf);
			}
			// Consumer<String> inputConsumer = rootView.getInputConsumer();
			// if (inputConsumer != null) {
			// 	inputConsumer.accept(binding);
			// }
		}
	}

	private void loop() {
		Attributes attr = terminal.enterRawMode();

		eventLoop.start();

		registerEventHandling();

		terminal.handle(Signal.WINCH, signal -> {
			log.debug("Handling signal {}", signal);
			dispatchWinch();
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
			eventLoop.stop();
			terminal.setAttributes(attr);
			terminal.trackMouse(Terminal.MouseTracking.Off);
			terminal.puts(Capability.keypad_local);
			terminal.puts(Capability.cursor_visible);
			display.update(Collections.emptyList(), 0);
		}
	}

	private boolean read(BindingReader bindingReader, KeyMap<String> keyMap) {
		String operation = bindingReader.readBinding(keyMap);
		log.debug("Read got operation {}", operation);
		if (operation == null) {
			return true;
		}
		switch (operation) {
			case OPERATION_EXIT:
				return true;
			case OPERATION_MOUSE_EVENT:
				mouseEvent();
				break;
			case OPERATION_KEY_EVENT:
				String lastBinding = bindingReader.getLastBinding();
				dispatchChar(lastBinding);
				break;
		}

		return false;
	}

	private void dispatchChar(String binding) {
		log.trace("Dispatching {} with {}", OPERATION_KEY_EVENT, binding);
		KeyEvent event = new KeyEvent(binding);
		Message<KeyEvent> message = MessageBuilder
			// .withPayload(binding)
			.withPayload(event)
			.setHeader(EventLoop.TYPE, EventLoop.Type.KEY)
			.build();
		eventLoop.dispatch(message);
	}

	private void dispatchMouse(MouseEvent event) {
		Message<MouseEvent> message = MessageBuilder
			.withPayload(event)
			.setHeader(EventLoop.TYPE, EventLoop.Type.MOUSE)
			.build();
		eventLoop.dispatch(message);
	}

    private void mouseEvent() {
        MouseEvent event = terminal.readMouseEvent();
		log.info("MOUSE: {}", event);
		dispatchMouse(event);
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
