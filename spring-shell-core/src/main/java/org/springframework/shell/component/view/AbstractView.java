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

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.jline.terminal.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.shell.component.view.geom.Rectangle;
import org.springframework.shell.component.view.listener.CompositeListener;
import org.springframework.shell.component.view.listener.CompositeShellMessageListener;
import org.springframework.shell.component.view.listener.ShellMessageListener;
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
	public BiFunction<MouseEvent, Consumer<View>, MouseEvent> getMouseHandler() {
		return (event, view) -> event;
	}

	@Override
	public BiConsumer<KeyEvent, Consumer<View>> getInputHandler() {
		return (event, focus) -> {
			focus.accept(this);
		};
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
