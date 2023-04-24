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

import org.assertj.core.api.AbstractAssert;

import org.springframework.shell.component.view.Screen;
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
		isNotNull();
		ScreenItem[][] content = actual.getContent();
		checkBounds(content, x, y, width, height);
		ScreenItem[][] borders = getBorders(content);
		ScreenItem[] topBorder = borders[0];
		ScreenItem[] rightBorder = borders[1];
		ScreenItem[] bottomBorder = borders[2];
		ScreenItem[] leftBorder = borders[3];
		if (topBorder.length != width) {
			failWithMessage("Top Border size doesn't match");
		}
		assertThat(topBorder).satisfies(b -> {
			assertThat(b).isNotNull().allMatch(sc -> sc.getType() == Screen.Type.BORDER);
		});
		assertThat(rightBorder).satisfies(b -> {
			assertThat(b).isNotNull().allMatch(sc -> sc.getType() == Screen.Type.BORDER);
		});
		assertThat(bottomBorder).satisfies(b -> {
			assertThat(b).isNotNull().allMatch(sc -> sc.getType() == Screen.Type.BORDER);
		});
		assertThat(leftBorder).satisfies(b -> {
			assertThat(b).isNotNull().allMatch(sc -> sc.getType() == Screen.Type.BORDER);
		});
		return this;
	}

	private ScreenItem[][] getBorders(ScreenItem[][] content) {
		ScreenItem[] topBorder = getTopBorder(content);
		ScreenItem[] rightBorder = getRightBorder(content);
		ScreenItem[] bottomBorder = getBottomBorder(content);
		ScreenItem[] leftBorder = getLeftBorder(content);
		return new ScreenItem[][] { topBorder, rightBorder, bottomBorder, leftBorder };
	}

	private ScreenItem[] getTopBorder(ScreenItem[][] content) {
		return content[0];
	}

	private ScreenItem[] getRightBorder(ScreenItem[][] content) {
		ScreenItem[] array = new ScreenItem[content[0].length];
		for (int i = 0; i < content.length; i++) {
			array[i] = content[i][content[i].length - 1];
		}
		return array;
	}

	private ScreenItem[] getBottomBorder(ScreenItem[][] content) {
		return content[content.length - 1];
	}

	private ScreenItem[] getLeftBorder(ScreenItem[][] content) {
		ScreenItem[] array = new ScreenItem[content[0].length];
		for (int i = 0; i < content.length; i++) {
			array[i] = content[i][0];
		}
		return array;
	}

	private void checkBounds(ScreenItem[][] content, int x, int y, int width, int height) {
		if (x < 0 || y < 0 || width < 1 || height < 1) {
			failWithMessage("Can't assert with negative bounded rectangle, was x=%s y=%s width=%s height=%s", x, y,
					width, height);
		}
	}
}
