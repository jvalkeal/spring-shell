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
import java.util.List;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.shell.component.view.View;
import org.springframework.shell.component.view.geom.HorizontalAlign;
import org.springframework.shell.component.view.geom.VerticalAlign;
import org.springframework.util.Assert;

public class DefaultScreenx implements Screenx {

	private final static Logger log = LoggerFactory.getLogger(DefaultScreenx.class);
	// private ScreenxItem[][] items;
	private DefaultScreenxItem[][] items;
	private boolean showCursor;
	private View.Position cursorPosition = new View.Position(0, 0);
	private int rows = 0;
	private int columns = 0;

	public DefaultScreenx() {
		this(0, 0);
	}

	public DefaultScreenx(int rows, int columns) {
		resize(rows, columns);
	}

	@Override
	public Screenx clip(int x, int y, int width, int height) {
		DefaultScreenx screen = new DefaultScreenx(height, width);
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				screen.items[i][j] = items[y + i][x + j];
				// screen.setContent(j, i, items[y + i][x + j]);
			}
		}
		return screen;
	}

	@Override
	public void setShowCursor(boolean showCursor) {
		this.showCursor = showCursor;
	}

	@Override
	public boolean isShowCursor() {
		return showCursor;
	}

	@Override
	public void setCursorPosition(View.Position cursorPosition) {
		this.cursorPosition = cursorPosition;
	}

	@Override
	public View.Position getCursorPosition() {
		return cursorPosition;
	}

	@Override
	public ScreenxItem[][] getItems() {
		return items;
	}

	@Override
	public void resize(int rows, int columns) {
		Assert.isTrue(rows > -1, "Cannot have negative rows size");
		Assert.isTrue(columns > -1, "Cannot have negative columns size");
		this.rows = rows;
		this.columns = columns;
		reset();
		log.trace("Screen reset rows={} cols={}", this.rows, this.columns);
	}


	public void setBackground(int x, int y, int color) {
		items[y][x].background = color;
	}

	public void addStyle(int x, int y, int style) {
		items[y][x].style |= style;
	}

	public void removeStyle(int x, int y, int style) {
		items[y][x].style &= ~style;
	}

	// @Override
	// public void setItem(int x, int y, ScreenxItem item) {
	// 	items[y][x] = item;
	// }

	public void reset() {
		this.items = new DefaultScreenxItem[rows][columns];
		for (int i = 0; i < rows; i++) {
			this.items[i] = new DefaultScreenxItem[columns];
			for (int j = 0; j < columns; j++) {
				this.items[i][j] = new DefaultScreenxItem();
			}
		}
	}

	@Override
	public void print(String text, int x, int y, int width) {
		for (int i = 0; i < text.length() && i < width; i++) {
			char c = text.charAt(i);
			this.items[y][x + i].content = Character.toString(c);
		}
	}

	@Override
	public void print(String text, View.Rectangle rect, HorizontalAlign hAlign, VerticalAlign vAlign) {
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

	private static char[] BOX_CHARS = new char[] { ' ', '╴', '╵', '┌', '╶', '─', '┐', '┬', '╷', '└', '│', '├', '┘', '┴',
			'┤', '┼' };

	@Override
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
			if (y >= items.length) {
				continue;
			}
			DefaultScreenxItem item = items[y][i];
			if (item == null) {
				item = new DefaultScreenxItem();
				items[y][i] = item;
			}
			if (i > x) {
				item.border |= ScreenxItem.BORDER_RIGHT;
			}
			if (i < x + width - 1) {
				item.border |= ScreenxItem.BORDER_LEFT;
			}
		}
	}

	public void printBorderVertical(int x, int y, int height) {
		for (int i = y; i < y + height; i++) {
			if (i < 0 || i >= rows) {
				continue;
			}
			if (x >= items[i].length) {
				continue;
			}
			DefaultScreenxItem item = items[i][x];
			if (item == null) {
				item = new DefaultScreenxItem();
				items[i][x] = item;
			}
			if (i > y) {
				item.border |= ScreenxItem.BORDER_BOTTOM;
			}
			if (i < y + height - 1) {
				item.border |= ScreenxItem.BORDER_TOP;
			}
		}
	}


	@Override
	public List<AttributedString> getScreenLines() {
		List<AttributedString> newLines = new ArrayList<>();
		for (int i = 0; i < items.length; i++) {
			AttributedStringBuilder builder = new AttributedStringBuilder();
			for (int j = 0; j < items[i].length; j++) {
				DefaultScreenxItem item = items[i][j];
				if (item != null) {
					AttributedStyle s = new AttributedStyle();
					// if (item.background > -1) {
					// 	s.background(item.getBackground());
					// }
					// s.foreground(item.getForeround());
					// if ((item.style & ScreenxItem.STYLE_BOLD) == ScreenxItem.STYLE_BOLD) {
					// 	s.bold();
					// }
					// s.faint();
					// s.italic();
					// s.underline();
					// s.blink();
					// s.inverse();
					// s.conceal();
					// s.crossedOut();
					// builder.
					if (item.getContent() != null){
						builder.append(item.getContent(), AttributedStyle.DEFAULT);
					}
					else if (item.getBorder() > 0) {
						builder.append(Character.toString(BOX_CHARS[item.getBorder()]), null);
					}
					else {
						builder.append(' ');
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

	private static class DefaultScreenxItem implements ScreenxItem {

		CharSequence content;
		int background;
		int style;
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
		public int getForeround() {
			throw new UnsupportedOperationException("Unimplemented method 'getForeround'");
		}

		@Override
		public int getStyle() {
			return style;
		}

	}
}
