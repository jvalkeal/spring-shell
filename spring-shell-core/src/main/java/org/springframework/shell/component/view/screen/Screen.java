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
package org.springframework.shell.component.view.screen;

import org.springframework.shell.component.view.geom.HorizontalAlign;
import org.springframework.shell.component.view.geom.Position;
import org.springframework.shell.component.view.geom.Rectangle;
import org.springframework.shell.component.view.geom.VerticalAlign;

/**
 * {@code Screen} is representing a virtual area which is sitting between a user
 * and lower level {@code jline} terminal providing convenient methods working
 * with visible content.
 *
 * @author Janne Valkealahti
 */
public interface Screen {

	/**
	 * Sets if cursor should be visible.
	 *
	 * @param show true if cursor should be visible
	 */
	void setShowCursor(boolean show);

	/**
	 * Gets if cursor is visible.
	 *
	 * @return true if cursor is visible
	 */
	boolean isShowCursor();

	/**
	 * Sets a cursor position.
	 *
	 * @param position new cursor position
	 */
	void setCursorPosition(Position position);

	/**
	 * Gets a cursor position.
	 *
	 * @return cursor position
	 */
	Position getCursorPosition();

	/**
	 * Gets two dimensional array of {@link ScreenItem}'s.
	 *
	 * @return two dimensional array of screen items
	 */
	ScreenItem[][] getItems();

	void print(String text, int x, int y, int width);

	void print(String text, int x, int y, int width, int color, int style);

	void print(String text, Rectangle rect, HorizontalAlign hAlign, VerticalAlign vAlign);

	void print(String text, Rectangle rect, HorizontalAlign hAlign, VerticalAlign vAlign, int color, int style);

	void printBorder(int x, int y, int width, int height);

	void resize(int rows, int columns);

	Screen clip(int x, int y, int width, int height);

	void setBackground(Rectangle rect, int color);
}
