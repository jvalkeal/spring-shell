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

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.shell.component.view.geom.HorizontalAlign;
import org.springframework.shell.component.view.geom.Position;
import org.springframework.shell.component.view.geom.Rectangle;
import org.springframework.shell.component.view.geom.VerticalAlign;
import org.springframework.util.Assert;

/**
 * Default implementation of a {@link Screen}.
 *
 * @author Janne Valkealahti
 */
public class DefaultScreen implements Screen, DisplayLines {

	private final static Logger log = LoggerFactory.getLogger(DefaultScreen.class);
	private DefaultScreenItem[][] items;
	private Map<Integer, DefaultScreenItem[][]> layerItems = new HashMap<>();
	private boolean showCursor;
	private Position cursorPosition = new Position(0, 0);
	private int rows = 0;
	private int columns = 0;

	public DefaultScreen() {
		this(0, 0);
	}

	public DefaultScreen(int rows, int columns) {
		resize(rows, columns);
	}

	@Override
	public Screen clip(int x, int y, int width, int height) {
		DefaultScreen screen = new DefaultScreen(height, width);
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				screen.items[i][j] = items[y + i][x + j];
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
	public void setCursorPosition(Position cursorPosition) {
		this.cursorPosition = cursorPosition;
	}

	@Override
	public Position getCursorPosition() {
		return cursorPosition;
	}

	@Override
	public ScreenItem[][] getItems() {
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

	@Override
	public void setBackground(Rectangle rect, int color) {
		for (int i = rect.y(); i < rect.y() + rect.height(); i++) {
			for (int j = rect.x(); j < rect.x() + rect.width(); j++) {
				setBackground(j, i, color);
			}
		}
	}

	public void addStyle(int x, int y, int style) {
		items[y][x].style |= style;
	}

	public void removeStyle(int x, int y, int style) {
		items[y][x].style &= ~style;
	}

	public void reset() {
		DefaultScreenItem[][] layer0 = layerItems.computeIfAbsent(0, l -> {
			return new DefaultScreenItem[rows][columns];
		});
		this.items = new DefaultScreenItem[rows][columns];
		for (int i = 0; i < rows; i++) {
			this.items[i] = new DefaultScreenItem[columns];
			layer0[i] = new DefaultScreenItem[columns];

			for (int j = 0; j < columns; j++) {
				this.items[i][j] = new DefaultScreenItem();
				layer0[i][j] = new DefaultScreenItem();
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
	public void print(String text, int x, int y, int width, int color, int style) {
		for (int i = 0; i < text.length() && i < width; i++) {
			char c = text.charAt(i);
			this.items[y][x + i].content = Character.toString(c);
			this.items[y][x + i].foreground = color;
			this.items[y][x + i].style = style;
		}
	}

	@Override
	public void print(String text, Rectangle rect, HorizontalAlign hAlign, VerticalAlign vAlign, int color, int style) {
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
		print(text, x, y, text.length(), color, style);
	}

	@Override
	public void print(String text, Rectangle rect, HorizontalAlign hAlign, VerticalAlign vAlign) {
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
			DefaultScreenItem item = items[y][i];
			if (item == null) {
				item = new DefaultScreenItem();
				items[y][i] = item;
			}
			if (i > x) {
				item.border |= ScreenItem.BORDER_RIGHT;
			}
			if (i < x + width - 1) {
				item.border |= ScreenItem.BORDER_LEFT;
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
			DefaultScreenItem item = items[i][x];
			if (item == null) {
				item = new DefaultScreenItem();
				items[i][x] = item;
			}
			if (i > y) {
				item.border |= ScreenItem.BORDER_BOTTOM;
			}
			if (i < y + height - 1) {
				item.border |= ScreenItem.BORDER_TOP;
			}
		}
	}


	@Override
	public List<AttributedString> getScreenLines() {
		List<AttributedString> newLines = new ArrayList<>();
		for (int i = 0; i < items.length; i++) {
			AttributedStringBuilder builder = new AttributedStringBuilder();
			for (int j = 0; j < items[i].length; j++) {
				DefaultScreenItem item = items[i][j];
				if (item != null) {
					AttributedStyle s = new AttributedStyle(AttributedStyle.DEFAULT);
					if (item.background > -1) {
						s = s.backgroundRgb(item.getBackground());
					}
					if (item.foreground > -1) {
						s = s.foregroundRgb(item.getForeground());
					}
					if (item.style > -1) {
						if ((item.style & ScreenItem.STYLE_BOLD) == ScreenItem.STYLE_BOLD) {
							s = s.bold();
						}
						if ((item.style & ScreenItem.STYLE_FAINT) == ScreenItem.STYLE_FAINT) {
							s = s.faint();
						}
						if ((item.style & ScreenItem.STYLE_ITALIC) == ScreenItem.STYLE_ITALIC) {
							s = s.italic();
						}
						if ((item.style & ScreenItem.STYLE_UNDERLINE) == ScreenItem.STYLE_UNDERLINE) {
							s = s.underline();
						}
						if ((item.style & ScreenItem.STYLE_BLINK) == ScreenItem.STYLE_BLINK) {
							s = s.blink();
						}
						if ((item.style & ScreenItem.STYLE_INVERSE) == ScreenItem.STYLE_INVERSE) {
							s = s.inverse();
						}
						if ((item.style & ScreenItem.STYLE_CONCEAL) == ScreenItem.STYLE_CONCEAL) {
							s = s.conceal();
						}
						if ((item.style & ScreenItem.STYLE_CROSSEDOUT) == ScreenItem.STYLE_CROSSEDOUT) {
							s = s.crossedOut();
						}
					}
					if (item.getContent() != null){
						builder.append(item.getContent(), s);
					}
					else if (item.getBorder() > 0) {
						builder.append(Character.toString(BOX_CHARS[item.getBorder()]), s);
					}
					else {
						builder.append(Character.toString(' '), s);
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
}
