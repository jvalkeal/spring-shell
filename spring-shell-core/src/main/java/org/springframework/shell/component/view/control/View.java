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

import org.springframework.lang.Nullable;
import org.springframework.shell.component.view.event.KeyHandler;
import org.springframework.shell.component.view.event.MouseHandler;
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
public interface View extends Control {

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
	 * Gets a {@link View} mouse {@link MouseHandler}. Can be {@code null} which
	 * indicates view will not handle any mouse events.
	 *
	 * @return a view mouse handler
	 */
	@Nullable
	MouseHandler getMouseHandler();

	/**
	 * Gets a {@link View} mouse {@link KeyHandler}. Can be {@code null} which
	 * indicates view will not handle any key events.
	 *
	 * @return a view mouse handler
	 */
	@Nullable
	KeyHandler getKeyHandler();

	/**
	 * Get composite listener used for registration.
	 *
	 * @return
	 */
	CompositeListener<ShellMessageListener> getMessageListeners();

}
