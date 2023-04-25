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

import java.util.function.Consumer;

/**
 * {@code View} is an interface representing something what can be drawn into
 * a {@link Screen} within its bounds.
 *
 * @author Janne Valkealahti
 */
public interface View {

	/**
	 * Draw view into {@link Screen}. This is a main access point to draw
	 * visible things in a screen.
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

	void focus();
	boolean hasFocus();

	/**
	 * Sets an input consumer for an {@code operation}.
	 *
	 * @param inputConsumer an input consumer
	 */
	void setInputConsumer(Consumer<String> inputConsumer);

	/**
	 * Gets an input consumer.
	 *
	 * @return the input consumer
	 */
	Consumer<String> getInputConsumer();

	/**
	 * Record representing coordinates {@code x}, {@code y} and its {@code width}
	 * and {@code height}.
	 */
	record Rectangle(int x, int y, int width, int height) {
	};

	/**
	 * Record representing dimensions {@code width} and {@code height}.
	 */
	record Dimension(int width, int height) {
	};
}