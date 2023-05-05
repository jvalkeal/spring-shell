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

import java.util.Arrays;

import org.assertj.core.api.AbstractAssert;

import org.springframework.shell.component.view.Screen.ScreenItem;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Asserts for {@link Screen}.
 *
 * @author Janne Valkealahti
 */
public class ScreenAssert extends AbstractAssert<ScreenAssert, Screen> {

	public ScreenAssert(Screen actual) {
		super(actual, ScreenAssert.class);
	}

	/**
	 * Verifies that a given bounded box is legal for a screen and that characters
	 * along border look like border characters.
	 *
	 * @param x a x position in a screen
	 * @param y a y position in a screen
	 * @param width  a width in a screen
	 * @param height a height in a screen
	 * @return this assertion object
	 */
	public ScreenAssert hasBorder(int x, int y, int width, int height) {
		return hasBorderType(x, y, width, height, true);
	}

	/**
	 * Verifies that a given bounded box is legal for a screen and that characters
	 * along border doesn't look like border characters.
	 *
	 * @param x a x position in a screen
	 * @param y a y position in a screen
	 * @param width  a width in a screen
	 * @param height a height in a screen
	 * @return this assertion object
	 */
	public ScreenAssert hasNoBorder(int x, int y, int width, int height) {
		return hasBorderType(x, y, width, height, false);
	}

	/**
	 * Verifies that a given text can be found from a screen coordinates following
	 * horizontal width.
	 *
	 * @param text a text to verify
	 * @param x a x position of a text
	 * @param y a y position of a text
	 * @param width a width of a text to check
	 * @return this assertion object
	 */
	public ScreenAssert hasHorizontalText(String text, int x, int y, int width) {
		isNotNull();
		ScreenItem[][] content = actual.getContent();
		checkBounds(content, x, y, width, 1);
		ScreenItem[] items = getHorizontalBorder(content, x, y, width);
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < items.length; i++) {
			if (items[i].content != null) {
				buf.append(items[i].content);
			}
		}
		String actualText = buf.toString();
		assertThat(actualText).isEqualTo(text);
		return this;
	}

	private ScreenAssert hasBorderType(int x, int y, int width, int height, boolean border) {
		isNotNull();
		ScreenItem[][] content = actual.getContent();
		checkBounds(content, x, y, width, height);
		ScreenItem[][] borders = getBorders(content, x, y, width, height);
		ScreenItem[] topBorder = borders[0];
		ScreenItem[] rightBorder = borders[1];
		ScreenItem[] bottomBorder = borders[2];
		ScreenItem[] leftBorder = borders[3];
		if (topBorder.length != width) {
			failWithMessage("Top Border size doesn't match");
		}
		assertThat(topBorder).allSatisfy(b -> {
			if (border) {
				assertThat(b).isNotNull();
				assertThat(b.getType()).isEqualTo(Screen.Type.BORDER);
			}
			else {
				if (b != null) {
					assertThat(b.getType()).isNotEqualTo(Screen.Type.BORDER);
				}
			}
		});
		assertThat(rightBorder).allSatisfy(b -> {
			if (border) {
				assertThat(b).isNotNull();
				assertThat(b.getType()).isEqualTo(Screen.Type.BORDER);
			}
			else {
				if (b != null) {
					assertThat(b.getType()).isNotEqualTo(Screen.Type.BORDER);
				}
			}
		});
		assertThat(bottomBorder).allSatisfy(b -> {
			if (border) {
				assertThat(b).isNotNull();
				assertThat(b.getType()).isEqualTo(Screen.Type.BORDER);
			}
			else {
				if (b != null) {
					assertThat(b.getType()).isNotEqualTo(Screen.Type.BORDER);
				}
			}
		});
		assertThat(leftBorder).allSatisfy(b -> {
			if (border) {
				assertThat(b).isNotNull();
				assertThat(b.getType()).isEqualTo(Screen.Type.BORDER);
			}
			else {
				if (b != null) {
					assertThat(b.getType()).isNotEqualTo(Screen.Type.BORDER);
				}
			}
		});
		return this;
	}

	private ScreenItem[][] getBorders(ScreenItem[][] content, int x, int y, int width, int height) {
		ScreenItem[] topBorder = getHorizontalBorder(content, y, x, width);
		ScreenItem[] rightBorder = getVerticalBorder(content, x + width - 1, y, height);
		ScreenItem[] bottomBorder = getHorizontalBorder(content, y + height - 1, x, width);
		ScreenItem[] leftBorder = getVerticalBorder(content, x, x, height);
		return new ScreenItem[][] { topBorder, rightBorder, bottomBorder, leftBorder };
	}

	private ScreenItem[] getHorizontalBorder(ScreenItem[][] content, int row, int start, int width) {
		return Arrays.copyOfRange(content[row], start, start + width);
	}

	private ScreenItem[] getVerticalBorder(ScreenItem[][] content, int column, int start, int height) {
		ScreenItem[] array = new ScreenItem[height];
		for (int i = 0; i < array.length; i++) {
			array[i] = content[start][column];
		}
		return array;
	}

	private void checkBounds(ScreenItem[][] content, int x, int y, int width, int height) {
		if (x < 0 || y < 0 || width < 1 || height < 1) {
			failWithMessage("Can't assert with negative bounded rectangle, was x=%s y=%s width=%s height=%s", x, y,
					width, height);
		}
		if (x >= content[0].length) {
			failWithMessage("Can't assert position x %s as width is %s", x, content[0].length);
		}
	}
}
