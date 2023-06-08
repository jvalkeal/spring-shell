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

import org.springframework.shell.component.view.screen.Screen;

public interface Control {

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

}
