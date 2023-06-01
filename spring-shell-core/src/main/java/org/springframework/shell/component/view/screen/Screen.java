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
	 * Gets a new instance of a {@link WriterBuilder}.
	 *
	 * @return a new writer builder
	 */
	WriterBuilder writerBuilder();

	void print(String text, int x, int y, int width);
	void print(String text, Rectangle rect, HorizontalAlign hAlign, VerticalAlign vAlign);
	void print(String text, Rectangle rect, HorizontalAlign hAlign, VerticalAlign vAlign, int color, int style);
	void printBorder(int x, int y, int width, int height);
	void resize(int rows, int columns);
	ScreenItem[][] getItems();
	Screen clip(int x, int y, int width, int height);

	/**
	 * Interface to write into a {@link Screen}. Contains convenient methods user is
	 * most likely to need to operate on a {@link Screen}.
	 */
	interface Writer {

		/**
		 * Write a text horizontally starting from a position defined by {@code x} and
		 * {@code y} within a bounds of a {@link Screen}.
		 *
		 * @param text the text to write
		 * @param x the x position
		 * @param y the y position
		 */
		void text(String text, int x, int y);

		/**
		 * Write a border with a given rectangle coordinates.
		 *
		 * @param x the x position
		 * @param y the y position
		 * @param width the rectangle width
		 * @param height the rectangle height
		 */
		void border(int x, int y, int width, int height);

		void background(Rectangle rect, int color);

		void text(String text, Rectangle rect, HorizontalAlign hAlign, VerticalAlign vAlign);
	}

	/**
	 * Builder interface for a {@link Writer}. Allows to defined settings a builder
	 * will operare on.
	 */
	interface WriterBuilder {

		/**
		 * Define a {@code z-index} this {@link Writer} operates on.
		 * {@code WriterBuilder} defaults on a layer index {@code 0}.
		 *
		 * @param index the z-index
		 * @return a writer builder for chaining
		 */
		WriterBuilder layer(int index);

		WriterBuilder color(int color);
		WriterBuilder style(int style);

		/**
		 * Build a {@link Writer}.
		 *
		 * @return a build writer
		 */
		Writer build();
	}
}
