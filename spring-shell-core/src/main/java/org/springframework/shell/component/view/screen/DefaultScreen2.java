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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.RowSet;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.shell.component.view.geom.HorizontalAlign;
import org.springframework.shell.component.view.geom.Position;
import org.springframework.shell.component.view.geom.Rectangle;
import org.springframework.shell.component.view.geom.VerticalAlign;
import org.springframework.shell.component.view.screen.Screen.Writer;
import org.springframework.shell.component.view.screen.Screen.WriterBuilder;
import org.springframework.util.Assert;

/**
 * Default implementation of a {@link Screen}.
 *
 * @author Janne Valkealahti
 */
public class DefaultScreen2 /*implements Screen, DisplayLines*/ {

	private final static Logger log = LoggerFactory.getLogger(DefaultScreen.class);
	// private DefaultScreenItem[][] items;
	private boolean showCursor;
	private Position cursorPosition = new Position(0, 0);
	private int rows = 0;
	private int columns = 0;

	public DefaultScreen2() {
		this(0, 0);
	}

	public DefaultScreen2(int rows, int columns) {
		resize(rows, columns);
	}

	// @Override
	public WriterBuilder writerBuilder() {
		return new DefaultWriterBuilder();

	}

	// @Override
	public void setShowCursor(boolean showCursor) {
		this.showCursor = showCursor;
	}

	// @Override
	public boolean isShowCursor() {
		return showCursor;
	}

	// @Override
	public void setCursorPosition(Position cursorPosition) {
		this.cursorPosition = cursorPosition;
	}

	// @Override
	public Position getCursorPosition() {
		return cursorPosition;
	}

	// @Override
	public void resize(int rows, int columns) {
		// Assert.isTrue(rows > -1, "Cannot have negative rows size");
		// Assert.isTrue(columns > -1, "Cannot have negative columns size");
		// this.rows = rows;
		// this.columns = columns;
		// reset();
		// log.trace("Screen reset rows={} cols={}", this.rows, this.columns);
	}

	/**
	 * Default private implementation of a {@link ScreenItem}.
	 */
	private static class DefaultScreenItem implements ScreenItem {

		CharSequence content;
		int foreground = -1;
		int background = -1;
		int style = -1;
		int border;

		@Override
		public CharSequence getContent() {
			return content;
		}

		@Override
		public int getBorder() {
			return border;
		}

		@Override
		public int getBackground() {
			return background;
		}

		@Override
		public int getForeground() {
			return foreground;
		}

		@Override
		public int getStyle() {
			return style;
		}

	}

	/**
	 * Default private implementation of a {@link WriterBuilder}.
	 */
	private class DefaultWriterBuilder implements WriterBuilder {

		int layer;

		@Override
		public Writer build() {
			return new DefaultWriter(layer);
		}

		@Override
		public WriterBuilder layer(int index) {
			this.layer = index;
			return this;
		}
	}


	public void reset() {
		// DefaultScreenItem[][] layer0 = layerItems.computeIfAbsent(0, l -> {
		// 	return new DefaultScreenItem[rows][columns];
		// });
		// this.items = new DefaultScreenItem[rows][columns];
		for (int i = 0; i < rows; i++) {
			// this.items[i] = new DefaultScreenItem[columns];
			// layer0[i] = new DefaultScreenItem[columns];

			for (int j = 0; j < columns; j++) {
				// this.items[i][j] = new DefaultScreenItem();
				// layer0[i][j] = new DefaultScreenItem();
			}
		}
	}

	private class Layer {
		DefaultScreenItem[][] items = new DefaultScreenItem[rows][columns];

		DefaultScreenItem getScreenItem(int x, int y) {
			if (items[y] == null) {
				items[y] = new DefaultScreenItem[columns];
			}
			if (items[y][x] == null) {
				items[y][x] = new DefaultScreenItem();
			}
			return items[y][x];
		}
	}

	private Map<Integer, Layer> layers = new HashMap<>();

	private Layer getLayer(int index) {
		Layer layer = layers.computeIfAbsent(index, l -> {
			return new Layer();
		});
		return layer;
	}

	public DefaultScreenItem[][] getScreenItems() {
		layers.keySet().stream().sorted().forEach(index -> {
			Layer layer = layers.get(index);

		});
		return null;
	}

	/**
	 * Default private implementation of a {@link Writer}.
	 */
	private class DefaultWriter implements Writer {

		int index;

		DefaultWriter(int index) {
			this.index = index;
		}

		@Override
		public void write(String text, int x, int y) {
			Layer layer = getLayer(index);
			for (int i = 0; i < text.length() && i < columns; i++) {
				char c = text.charAt(i);
				DefaultScreenItem item = layer.getScreenItem(x, y);
				item.content = Character.toString(c);
			}

		}


	}
}
