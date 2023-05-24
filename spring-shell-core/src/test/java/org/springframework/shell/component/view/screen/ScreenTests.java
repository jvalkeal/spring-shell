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

import org.junit.jupiter.api.Test;

import org.springframework.shell.component.view.AbstractViewTests;
import org.springframework.shell.component.view.geom.HorizontalAlign;
import org.springframework.shell.component.view.geom.Rectangle;
import org.springframework.shell.component.view.geom.VerticalAlign;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScreenTests extends AbstractViewTests {

	@Test
	void zeroDataSizeDoesntBreak() {
		assertThat(screen0x0.getItems()).isEmpty();
		assertThat(screen0x0.getScreenLines()).isEmpty();
	}

	@Test
	void cantResizeNegative() {
		assertThatThrownBy(() -> {
			screen0x0.resize(-1, 0);
		}).hasMessageContaining("negative rows");
		assertThatThrownBy(() -> {
			screen0x0.resize(0, -1);
		}).hasMessageContaining("negative columns");
	}

	@Test
	void printInBoxShows() {
		screen10x10.printBorder(0, 0, 10, 10);
		assertThat(forScreen(screen10x10)).hasBorder(0, 0, 10, 10);
	}

	@Test
	void printsText() {
		screen24x80.print("text", 0, 0, 4);
		assertThat(forScreen(screen24x80)).hasHorizontalText("text", 0, 0, 4);
	}

	@Test
	void printsTextAlign() {
		Rectangle rect = new Rectangle(1, 1, 10, 10);
		screen24x80.print("text", rect, HorizontalAlign.CENTER, VerticalAlign.CENTER);
		assertThat(forScreen(screen24x80)).hasHorizontalText("text", 3, 5, 4);
	}

	@Test
	void printsTextAlignInOneRowRect() {
		Rectangle rect = new Rectangle(1, 1, 10, 1);
		screen24x80.print("text", rect, HorizontalAlign.CENTER, VerticalAlign.CENTER);
		assertThat(forScreen(screen24x80)).hasHorizontalText("text", 3, 1, 4);
	}

}
