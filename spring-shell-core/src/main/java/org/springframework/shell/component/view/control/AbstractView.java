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

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.shell.component.view.event.EventLoop;
import org.springframework.shell.component.view.event.KeyEvent;
import org.springframework.shell.component.view.event.KeyHandler;
import org.springframework.shell.component.view.event.MouseHandler;
import org.springframework.shell.component.view.event.KeyEvent.KeyType;
import org.springframework.shell.component.view.geom.Rectangle;
import org.springframework.shell.component.view.listener.CompositeListener;
import org.springframework.shell.component.view.listener.CompositeShellMessageListener;
import org.springframework.shell.component.view.listener.ShellMessageListener;
import org.springframework.shell.component.view.message.ShellMessageBuilder;
import org.springframework.shell.component.view.screen.Screen;

/**
 * Base implementation of a {@link View}.
 *
 * @author Janne Valkealahti
 */
public abstract class AbstractView implements View {

	private final static Logger log = LoggerFactory.getLogger(AbstractView.class);
	private int x = 0;
	private int y = 0;
	private int width = 0;
	private int height = 0;
	private BiFunction<Screen, Rectangle, Rectangle> drawFunction;
	private boolean hasFocus;
	private final CompositeShellMessageListener messageListerer = new CompositeShellMessageListener();
	private int layer;
	private EventLoop eventLoop;
	private Map<String, Runnable> viewCommands = new HashMap<>();
	private Map<KeyType, String> viewBindings = new HashMap<>();

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

	@Override
	public MouseHandler getMouseHandler() {
		return null;
	}

	/**
	 * Handles keys by dispatching registered command runnable into an event loop.
	 * Override to change default behaviour.
	 */
	@Override
	public KeyHandler getKeyHandler() {
		KeyHandler handler = args -> {
			KeyEvent event = args.event();
			boolean consumed = false;
			KeyType key = event.key();
			if (key != null) {
				String command = getViewBindings().get(key);
				consumed = dispatchRunCommand(command);
			}
			return KeyHandler.resultOf(event, consumed, null);
		};
		return handler;
	}

	@Override
	public CompositeListener<ShellMessageListener> getMessageListeners() {
		return messageListerer;
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
	protected void addCommand(String viewCommand, Runnable runnable) {
		viewCommands.put(viewCommand, runnable);
	}

	/**
	 * Register a {@link KeyType} with a {@code view command}.
	 *
	 * @param keyType the key type
	 * @param viewCommand the view command
	 */
	protected void addKeyBinding(KeyType keyType, String viewCommand) {
		viewBindings.put(keyType, viewCommand);
	}

	/**
	 * Register {@link Runnable} to get handled with a {@link KeyType} and a
	 * {@code view command}.
	 *
	 * @param keyType the key type
	 * @param viewCommand the view command
	 * @param runnable the runnable
	 * @see #addCommand(String, Runnable)
	 * @see #addKeyBinding(KeyType, String)
	 */
	protected void addKeyBindingCommand(KeyType keyType, String viewCommand, Runnable runnable) {
		addCommand(viewCommand, runnable);
		addKeyBinding(keyType, viewCommand);
	}

	/**
	 * Get view bindings.
	 *
	 * @return view bindings
	 */
	protected Map<KeyType, String> getViewBindings() {
		return viewBindings;
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
	 * Takes a view command and matches it against registered {@link Runnable} and
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
		Runnable runnable = viewCommands.get(command);
		if (runnable != null) {
			Message<Runnable> message = ShellMessageBuilder.withPayload(runnable).setEventType(EventLoop.Type.TASK).build();
			dispatch(message);
			return true;
		}
		return false;
	}

	/**
	 * Gets a {@link ShellMessageListener} which can be used to dispatch an event.
	 *
	 * @return a shell message listener
	 */
	protected ShellMessageListener getShellMessageListener() {
		return messageListerer;
	}

	/**
	 * Component internal drawing method. Implementing classes needs to define this
	 * method to draw something into a {@link Screen}.
	 *
	 * @param screen the screen
	 */
	protected abstract void drawInternal(Screen screen);
}
