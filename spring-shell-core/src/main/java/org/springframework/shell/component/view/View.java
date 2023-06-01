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

import org.springframework.shell.component.view.event.KeyEvent;
import org.springframework.shell.component.view.geom.Rectangle;
import org.springframework.shell.component.view.listener.CompositeListener;
import org.springframework.shell.component.view.listener.ShellMessageListener;
import org.springframework.shell.component.view.screen.Screen;

/**
 * {@code View} is the interface for all {@code Views} on the {@link Screen} and
 * represents a visible element that can render itself and contains zero or more
 * nested {@code Views}.
 *
 * @author Janne Valkealahti
 */
public interface View {

	/**
	 * Draw view into {@link Screen}. This is a main access point to draw
	 * visible things into a screen.
	 *
	 * @param screen the screen
	 */
	void draw(Screen screen);

	/**
	 * Sets bounded box where this {@link View} should operate.
	 *
	 * @param x a x coord of a bounded box
	 * @param y an y coord of a bounded box
	 * @param width a width of a bounded box
	 * @param height a height of a bounded box
	 */
	void setRect(int x, int y, int width, int height);

	/**
	 * Gets rectanle of a bounded box for this {@link View}.
	 *
	 * @return the rectanle of a bounded box
	 */
	Rectangle getRect();

	/**
	 * Sets a layer index this {@code View} operates on.
	 *
	 * @param index the layer index
	 */
	void setLayer(int index);

	/**
	 * Called when {@code View} gets or loses a focus.
	 *
	 * @param view the view receiving focus
	 * @param focus flag if focus is received
	 */
	void focus(View view, boolean focus);

	/**
	 * Gets if this {@code View} has a focus.
	 *
	 * @return true if view has a focus
	 */
	boolean hasFocus();

	/**
	 *
	 * @return
	 */
	BiFunction<MouseEvent, Consumer<View>, MouseEvent> getMouseHandler();

	/**
	 *
	 * @return
	 */
	BiConsumer<KeyEvent, Consumer<View>> getInputHandler();

	/**
	 * Get composite listener used for registration.
	 *
	 * @return
	 */
	CompositeListener<ShellMessageListener> getMessageListeners();

}
