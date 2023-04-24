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

import org.springframework.shell.component.view.View.Dimension;
import org.springframework.util.Assert;

/**
 * {@code Screen} represents a virtual area bounded by {@code rows} and {@code columns}.
 * Think it as a shell screen you want to draw or write something.
 *
 * @author Janne Valkealahti
 */
public class Screen {

	private int rows = 0;
	private int columns = 0;
	private ScreenItem[][] content;

	public Screen() {
		this(0, 0);
	}

	public Screen(int rows, int columns) {
		resize(rows, columns);
	}

	public void resize(int rows, int columns) {
		Assert.isTrue(rows > -1, "Cannot have negative rows size");
		Assert.isTrue(columns > -1, "Cannot have negative columns size");
		this.rows = rows;
		this.columns = columns;
		reset();
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

	static int BORDER_LEFT = 1;
	static int BORDER_TOP = 2;
	static int BORDER_RIGHT = 4;
	static int BORDER_BOTTOM = 8;

	static char[] boxc = new char[] { ' ', '╴', '╵', '┌', '╶', '─', '┐', '┬', '╷', '└', '│', '├', '┘', '┴', '┤', '┼' };

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
					// ─ │ ┌ ┐ └ ┘ ├ ┤ ┬ ┴ ┼
					else if (item.type == Type.BORDER) {
						builder.append(boxc[item.borderx]);
						// if (item.border[0] == Border.HORIZONTAL && item.border[1] == Border.VERTICAL) {
						// 	if (i == 0 && j == 0) {
						// 		builder.append('┌');
						// 	}
						// 	else if (i == 0 && j == content[i].length - 1) {
						// 		builder.append('┐');
						// 	}
						// 	else if (i == content.length - 1 && j == content[i].length - 1) {
						// 		builder.append('┘');
						// 	}
						// 	else if (i == content.length - 1 && j == 0) {
						// 		builder.append('└');
						// 	}
						// 	else {
						// 		builder.append('┼');
						// 	}
						// }
						// else if (item.border[0] == Border.HORIZONTAL) {
						// 	builder.append('─');
						// }
						// else if (item.border[1] == Border.VERTICAL) {
						// 	builder.append('│');
						// }
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
		printBorderHorizontal(x, y, width, Border.HORIZONTAL);
		printBorderHorizontal(x, y + height - 1, width, Border.HORIZONTAL);
		printBorderVertical(x, y, height, Border.VERTICAL);
		printBorderVertical(x + width - 1, y, height, Border.VERTICAL);
	}

	public void printBorderHorizontal(int x, int y, int width, Border border) {
		for (int i = x; i < x + width; i++) {
			ScreenItem item = content[y][i];
			if (item == null) {
				item = ScreenItem.border();
				content[y][i] = item;
			}
			item.border[0] = Border.HORIZONTAL;
			if (i == x) {
				item.borderx |= BORDER_LEFT;
			}
			else if (i == x + width - 1) {
				item.borderx |= BORDER_RIGHT;
			}
			else {
				item.borderx |= BORDER_LEFT;
				item.borderx |= BORDER_RIGHT;
			}
		}
	}

	// ─ │ ┌ ┐ └ ┘ ├ ┤ ┬ ┴ ┼

	public void printBorderVertical(int x, int y, int height, Border border) {
		for (int i = y; i < y + height; i++) {
			ScreenItem item = content[i][x];
			if (item == null) {
				item = ScreenItem.border();
				content[i][x] = item;
			}
			item.border[1] = Border.VERTICAL;
			if (i == y) {
				item.borderx |= BORDER_TOP;
			}
			else if (i == y + height - 1) {
				item.borderx |= BORDER_BOTTOM;
			}
			else {
				item.borderx |= BORDER_TOP;
				item.borderx |= BORDER_BOTTOM;
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

	public enum Border {
		HORIZONTAL,
		VERTICAL,
		// TOPLEFT,
		// TOPRIGHT,
		// BOTTOMLEFT,
		// BOTTOMRIGHT
	}

	public static class ScreenItem {

		Type type;
		CharSequence content;
		AttributedStyle style;
		Border[] border = new Border[2];
		int borderx = 0;

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
	}

	// public record ScreenItem(Type type, CharSequence content, AttributedStyle style) {

	// 	public static ScreenItem of(char c) {
	// 		return new ScreenItem(Type.TEXT, new String(new char[]{c}), null);
	// 	}

	// 	public static ScreenItem border() {
	// 		return new ScreenItem(Type.BORDER, null, null);
	// 	}

	// 	public static ScreenItem of(CharSequence content) {
	// 		return new ScreenItem(Type.TEXT, content, null);
	// 	}
	// }
}
