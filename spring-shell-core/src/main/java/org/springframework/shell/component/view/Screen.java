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

import java.util.ArrayList;
import java.util.List;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.shell.component.view.View.Dimension;
import org.springframework.shell.component.view.View.Position;
import org.springframework.shell.component.view.View.Rectangle;
import org.springframework.util.Assert;

/**
 * {@code Screen} represents a virtual area bounded by {@code rows} and {@code columns}.
 * Think it as a shell screen you want to draw or write something.
 *
 * @author Janne Valkealahti
 */
public class Screen {

	private final static Logger log = LoggerFactory.getLogger(Screen.class);
	private static final int BORDER_LEFT = 1;
	private static final int BORDER_TOP = 2;
	private static final int BORDER_RIGHT = 4;
	private static final int BORDER_BOTTOM = 8;
	private static char[] BOX_CHARS = new char[] { ' ', '╴', '╵', '┌', '╶', '─', '┐', '┬', '╷', '└', '│', '├', '┘', '┴',
			'┤', '┼' };

	private int rows = 0;
	private int columns = 0;
	private ScreenItem[][] content;
	private boolean showCursor;
	private Position cursorPosition = new Position(0, 0);

	public Screen() {
		this(0, 0);
	}

	public Screen(int rows, int columns) {
		resize(rows, columns);
	}

	public Screen clip(int x, int y, int width, int height) {
		Screen screen = new Screen(height, width);
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				screen.setContent(j, i, content[y + i][x + j]);
			}
		}
		return screen;
	}

	public void setShowCursor(boolean showCursor) {
		this.showCursor = showCursor;
	}

	public boolean isShowCursor() {
		return showCursor;
	}

	public void setCursorPosition(Position cursorPosition) {
		this.cursorPosition = cursorPosition;
	}

	public Position getCursorPosition() {
		return cursorPosition;
	}

	public void resize(int rows, int columns) {
		Assert.isTrue(rows > -1, "Cannot have negative rows size");
		Assert.isTrue(columns > -1, "Cannot have negative columns size");
		this.rows = rows;
		this.columns = columns;
		reset();
		log.trace("Screen reset rows={} cols={}", this.rows, this.columns);
	}

	public Dimension getSize() {
		return new Dimension(columns, rows);
	}

	public void reset() {
		this.content = new ScreenItem[rows][columns];
		for (int i = 0; i < rows; i++) {
			this.content[i] = new ScreenItem[columns];
			for (int j = 0; j < columns; j++) {
				this.content[i][j] = null;
			}
		}
	}

	public void setContent(int x, int y, ScreenItem item) {
		content[y][x] = item;
	}

	public ScreenItem[][] getContent() {
		return content;
	}

	public void print(String text, int x, int y, int width) {
		for (int i = 0; i < text.length() && i < width; i++) {
			char c = text.charAt(i);
			setContent(x + i, y, ScreenItem.of(c));
		}
	}

	public enum HorizontalAlign {
		LEFT, CENTER, RIGHT,
	}

	public enum VerticalAlign {
		TOP, CENTER, BOTTOM
	}

	public void printx(String text, Rectangle rect, HorizontalAlign hAlign, VerticalAlign vAlign) {
		int x = rect.x();
		if (hAlign == HorizontalAlign.CENTER) {
			x = (x + rect.width()) / 2;
			x = x - text.length() / 2;
		}
		else if (hAlign == HorizontalAlign.RIGHT) {
			x = x + rect.width() - text.length();
		}
		int y = rect.y();
		if (vAlign == VerticalAlign.CENTER) {
			y = (y + rect.height()) / 2;
		}
		else if (vAlign == VerticalAlign.BOTTOM) {
			y = y + rect.height() - 1;
		}
		print(text, x, y, text.length());
	}

	public List<AttributedString> getScreenLines() {
		List<AttributedString> newLines = new ArrayList<>();
		for (int i = 0; i < content.length; i++) {
			AttributedStringBuilder builder = new AttributedStringBuilder();
			for (int j = 0; j < content[i].length; j++) {
				ScreenItem item = content[i][j];
				if (item != null) {
					if (item.type == Type.TEXT) {
						builder.append(content[i][j].getContent(), content[i][j].getStyle());
					}
					else if (item.type == Type.BORDER) {
						builder.append(BOX_CHARS[item.getBorder()]);
					}
				}
				else {
					builder.append(' ');
				}
			}
			newLines.add(builder.toAttributedString());
		}
		return newLines;
	}

	public void printBorder(int x, int y, int width, int height) {
		log.trace("PrintBorder rows={}, columns={}, x={}, y={}, width={}, height={}", this.rows, this.columns, x, y,
				width, height);
		printBorderHorizontal(x, y, width);
		printBorderHorizontal(x, y + height - 1, width);
		printBorderVertical(x, y, height);
		printBorderVertical(x + width - 1, y, height);
	}

	public void printBorderHorizontal(int x, int y, int width) {
		for (int i = x; i < x + width; i++) {
			if (i < 0 || i >= columns) {
				continue;
			}
			if (y >= content.length) {
				continue;
			}
			ScreenItem item = content[y][i];
			if (item == null) {
				item = ScreenItem.border();
				content[y][i] = item;
			}
			if (i > x) {
				item.addBorder(BORDER_RIGHT);
			}
			if (i < x + width - 1) {
				item.addBorder(BORDER_LEFT);
			}
		}
	}

	public void printBorderVertical(int x, int y, int height) {
		for (int i = y; i < y + height; i++) {
			if (i < 0 || i >= rows) {
				continue;
			}
			if (x >= content[i].length) {
				continue;
			}
			ScreenItem item = content[i][x];
			if (item == null) {
				item = ScreenItem.border();
				content[i][x] = item;
			}
			if (i > y) {
				item.addBorder(BORDER_BOTTOM);
			}
			if (i < y + height - 1) {
				item.addBorder(BORDER_TOP);
			}
		}
	}

	/**
	 *
	 */
	public enum Type {
		TEXT,
		BORDER
	}

	public static class ScreenItem {

		Type type;
		CharSequence content;
		AttributedStyle style;
		private int border = 0;

		public ScreenItem(Type type, CharSequence content, AttributedStyle style) {
			this.type = type;
			this.content = content;
			this.style = style;
		}

		public static ScreenItem of(char c) {
			return new ScreenItem(Type.TEXT, new String(new char[]{c}), null);
		}

		public static ScreenItem border() {
			return new ScreenItem(Type.BORDER, null, null);
		}

		public static ScreenItem of(CharSequence content) {
			return new ScreenItem(Type.TEXT, content, null);
		}

		public Type getType() {
			return type;
		}

		public CharSequence getContent() {
			return content;
		}

		public AttributedStyle getStyle() {
			return style;
		}

		public int getBorder() {
			return border;
		}

		public void setBorder(int border) {
			this.border = border;
		}

		public void addBorder(int border) {
			this.border |= border;
		}
	}
}
