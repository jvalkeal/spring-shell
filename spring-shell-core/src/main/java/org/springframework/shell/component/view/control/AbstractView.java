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
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.jline.terminal.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.Disposables;

import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.shell.component.view.event.EventLoop;
import org.springframework.shell.component.view.event.KeyEvent;
import org.springframework.shell.component.view.event.KeyEvent.KeyType;
import org.springframework.shell.component.view.event.KeyHandler;
import org.springframework.shell.component.view.event.MouseBinding;
import org.springframework.shell.component.view.event.MouseBindingConsumer;
import org.springframework.shell.component.view.event.MouseBindingConsumerArgs;
import org.springframework.shell.component.view.event.MouseHandler;
import org.springframework.shell.component.view.geom.Rectangle;
import org.springframework.shell.component.view.message.ShellMessageBuilder;
import org.springframework.shell.component.view.screen.Screen;

/**
 * Base implementation of a {@link View} and its parent interface
 * {@link Control} providing some common functionality for implementations.
 *
 * @author Janne Valkealahti
 */
public abstract class AbstractView implements View {

	private final static Logger log = LoggerFactory.getLogger(AbstractView.class);
	private final Disposable.Composite disposables = Disposables.composite();
	private int x = 0;
	private int y = 0;
	private int width = 0;
	private int height = 0;
	private BiFunction<Screen, Rectangle, Rectangle> drawFunction;
	private boolean hasFocus;
	private int layer;
	private EventLoop eventLoop;
	private Map<String, Runnable> keyCommands = new HashMap<>();
	private Map<KeyType, String> keyBindings = new HashMap<>();
	private Map<MouseBinding, MouseBindingValue> mouseBindings = new HashMap<>();
	private Map<String, MouseBindingConsumer> mouseCommands = new HashMap<>();

	public AbstractView() {
		init();
	}

	/**
	 * Register {@link Disposable} to get disposed when view terminates.
	 *
	 * @param disposable a disposable to dispose
	 */
	protected void onDestroy(Disposable disposable) {
		disposables.add(disposable);
	}

	/**
	 * Cleans running state of a {@link View} so that it can be left to get garbage
	 * collected.
	 */
	public void destroy() {
		disposables.dispose();
	}

	/**
	 * Initialize a view. Mostly reserved for future use and simply calls
	 * {@link #initInternal()}.
	 *
	 * @see #initInternal()
	 */
	protected final void init() {
		initInternal();
	}

	/**
	 * Internal init method called from {@link #init()}. Override to do something
	 * usefull. Typically key and mousebindings are registered from this method.
	 */
	protected void initInternal() {
	}

	@Override
	public void setRect(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	@Override
	public Rectangle getRect() {
		return new Rectangle(x, y, width, height);
	}

	@Override
	public void setLayer(int index) {
		this.layer = index;
	}

	protected int getLayer() {
		return layer;
	}

	@Override
	public final void draw(Screen screen) {
		drawInternal(screen);
	}

	/**
	 * Component internal drawing method. Implementing classes needs to define this
	 * method to draw something into a {@link Screen}.
	 *
	 * @param screen the screen
	 */
	protected abstract void drawInternal(Screen screen);

	@Override
	public void focus(View view, boolean focus) {
		log.debug("Focus view={} focus={}", view, focus);
		if (view == this && focus) {
			hasFocus = true;
		}
		if (!focus) {
			hasFocus = false;
		}
	}

	@Override
	public boolean hasFocus() {
		return hasFocus;
	}

	/**
	 * Handles mouse events by dispatching registered consumers into an event loop.
	 * Override to change default behaviour.
	 */
	@Override
	public MouseHandler getMouseHandler() {
		log.trace("getMouseHandler() {}", this);
		MouseHandler handler = args -> {
			MouseEvent event = args.event();
			MouseBinding binding = MouseBinding.of(event);
			View view = null;
			MouseBindingValue mouseBindingValue = getMouseBindings().get(binding);
			if (mouseBindingValue != null) {
				if (mouseBindingValue.mousePredicate().test(event)) {
					view = this;
					String command = mouseBindingValue.mouseCommand();
					if (command != null) {
						// view = this;
						dispatchConsumerCommand(command, event);
					}
				}
			}
			return MouseHandler.resultOf(args.event(), view != null, view, this);
		};
		return handler;
	}

	/**
	 * Handles keys by dispatching registered command runnable into an event loop.
	 * Override to change default behaviour.
	 */
	@Override
	public KeyHandler getKeyHandler() {
		log.trace("getKeyHandler() {}", this);
		KeyHandler handler = args -> {
			KeyEvent event = args.event();
			boolean consumed = false;
			KeyType key = event.key();
			if (key != null) {
				String command = getKeyBindings().get(key);
				consumed = dispatchRunCommand(command);
			}
			return KeyHandler.resultOf(event, consumed, null);
		};
		return handler;
	}

	/**
	 * Sets a callback function which is invoked after a {@link View} has been
	 * drawn.
	 *
	 * @param drawFunction the draw function
	 */
	public void setDrawFunction(BiFunction<Screen, Rectangle, Rectangle> drawFunction) {
		this.drawFunction = drawFunction;
	}

	/**
	 * Gets a draw function.
	 *
	 * @return null if function is not set
	 * @see #setDrawFunction(BiFunction)
	 */
	public BiFunction<Screen, Rectangle, Rectangle> getDrawFunction() {
		return drawFunction;
	}

	/**
	 * Set an {@link EventLoop}.
	 *
	 * @param eventLoop the event loop
	 */
	public void setEventLoop(@Nullable EventLoop eventLoop) {
		this.eventLoop = eventLoop;
	}

	/**
	 * Get an {@link EventLoop}.
	 *
	 * @return event loop
	 */
	protected EventLoop getEventLoop() {
		return eventLoop;
	}

	/**
	 * Register a {code view command} with a {@link Runnable}.
	 *
	 * @param viewCommand the view command
	 * @param runnable the runnable
	 */
	protected void registerRunnableCommand(String viewCommand, Runnable runnable) {
		keyCommands.put(viewCommand, runnable);
	}

	/**
	 * Register a {@link KeyType} with a {@code view command}.
	 *
	 * @param keyType the key type
	 * @param viewCommand the view command
	 */
	protected void registerKeyBinding(KeyType keyType, String viewCommand) {
		keyBindings.put(keyType, viewCommand);
	}

	/**
	 * Register mouse binding with a {@code mouse command}.
	 *
	 * @param type the mouse event type
	 * @param button the mouse event button
	 * @param modifiers the mouse event modifiers
	 * @param mouseCommand the mouse command
	 * @param mousePredicate the mouse event predicate
	 */
	protected void registerMouseBinding(MouseEvent.Type type, MouseEvent.Button button,
			EnumSet<MouseEvent.Modifier> modifiers, String mouseCommand, Predicate<MouseEvent> mousePredicate) {
		mouseBindings.put(new MouseBinding(type, button, modifiers), new MouseBindingValue(mouseCommand, mousePredicate));
	}

	/**
	 * Register mouse binding with a {@code mouse command}.
	 *
	 * @param type the mouse event type
	 * @param button the mouse event button
	 * @param modifiers the mouse event modifiers
	 * @param mouseCommand the mouse command
	 */
	protected void registerMouseBinding(MouseEvent.Type type, MouseEvent.Button button,
			EnumSet<MouseEvent.Modifier> modifiers, String mouseCommand) {
		Predicate<MouseEvent> predicate = event -> {
			int x = event.getX();
			int y = event.getY();
			return getRect().contains(x, y);
		};
		registerMouseBinding(type, button, modifiers, mouseCommand, predicate);
	}


	record MouseBindingValue(String mouseCommand, Predicate<MouseEvent> mousePredicate){}

	/**
	 * Register a {code mouse command} with a {@link MouseBindingConsumer}.
	 *
	 * @param mouseCommand the mouse command
	 * @param consumer the mouse binding consumer
	 */
	protected void registerMouseBindingConsumerCommand(String mouseCommand, MouseBindingConsumer consumer) {
		mouseCommands.put(mouseCommand, consumer);
	}

	/**
	 * Get mouse bindings.
	 *
	 * @return mouse bindings
	 */
	protected Map<MouseBinding, MouseBindingValue> getMouseBindings() {
		return mouseBindings;
	}

	/**
	 * Get key bindings.
	 *
	 * @return key bindings
	 */
	protected Map<KeyType, String> getKeyBindings() {
		return keyBindings;
	}

	/**
	 * Dispatch a {@link Message} into an event loop.
	 *
	 * @param message the message to dispatch
	 */
	protected void dispatch(Message<?> message) {
		if (eventLoop != null) {
			eventLoop.dispatch(message);
		}
		else {
			log.warn("Can't dispatch message {} as eventloop is not set", message);
		}
	}

	/**
	 * Takes a {@code view command} and matches it against registered {@link Runnable} and
	 * then schedules that to get executed in an event loop. Returns {@code true}
	 * if a message was dispatched.
	 *
	 * @param command the view command
	 * @return true if command was handled with matching registration
	 */
	protected boolean dispatchRunCommand(String command) {
		if (eventLoop == null) {
			return false;
		}
		Runnable runnable = keyCommands.get(command);
		if (runnable != null) {
			Message<Runnable> message = ShellMessageBuilder
				.withPayload(runnable)
				.setEventType(EventLoop.Type.TASK)
				.build();
			dispatch(message);
			return true;
		}
		return false;
	}

	/**
	 * Takes a {@code view command} and {@link MouseEvent} then matches it against
	 * registered {@link MouseBindingConsumer} and then schedules that to get
	 * executed in an event loop. Returns {@code true} if a message was dispatched.
	 *
	 * @param command the view command
	 * @param event the mouse event
	 * @return true if command was handled with matching registration
	 */
	protected boolean dispatchConsumerCommand(String command, MouseEvent event) {
		if (eventLoop == null) {
			return false;
		}
		MouseBindingConsumer consumer = mouseCommands.get(command);
		if (consumer != null) {
			Message<MouseBindingConsumerArgs> message = ShellMessageBuilder
				.withPayload(new MouseBindingConsumerArgs(consumer, event))
				.setEventType(EventLoop.Type.TASK)
				.build();
			dispatch(message);
			return true;
		}
		return false;
	}

}
