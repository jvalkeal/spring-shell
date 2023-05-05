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

import org.assertj.core.api.AssertProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.shell.component.view.BoxView;
import org.springframework.shell.component.view.GridView;
import org.springframework.shell.component.view.Screen;

import static org.assertj.core.api.Assertions.assertThat;

class GridViewTests {

	private Screen screen;

	@BeforeEach
	void setup() {
		screen = new Screen(24, 80);
	}

	@Test
	void hasBordersWith1x1() {
		BoxView box1 = new BoxView();

		GridView grid = new GridView();
		grid.borders = true;
		grid.setRowSize(0);
		grid.setColumnSize(0);
		grid.setShowBorder(false);
		grid.addItem(box1, 0, 0, 1, 1, 0, 0);

		grid.setRect(0, 0, 80, 24);
		grid.draw(screen);

		assertThat(forScreen(screen)).hasBorder(0, 0, 80, 24);
	}

	@Test
	void hasBordersWith1x2() {
		BoxView box1 = new BoxView();
		BoxView box2 = new BoxView();

		GridView grid = new GridView();
		grid.borders = true;
		grid.setRowSize(0);
		grid.setColumnSize(0, 0);
		grid.setShowBorder(false);
		grid.addItem(box1, 0, 0, 1, 1, 0, 0);
		grid.addItem(box2, 0, 1, 1, 1, 0, 0);

		grid.setRect(0, 0, 80, 24);
		grid.draw(screen);

		assertThat(forScreen(screen)).hasBorder(0, 0, 80, 24);
		assertThat(forScreen(screen)).hasBorder(0, 0, 39, 24);
	}

	@Test
	void hasBordersWith2x1() {
		BoxView box1 = new BoxView();
		BoxView box2 = new BoxView();

		GridView grid = new GridView();
		grid.borders = true;
		grid.setRowSize(0, 0);
		grid.setColumnSize(0);
		grid.setShowBorder(false);
		grid.addItem(box1, 0, 0, 1, 1, 0, 0);
		grid.addItem(box2, 1, 0, 1, 1, 0, 0);

		grid.setRect(0, 0, 80, 24);
		grid.draw(screen);

		assertThat(forScreen(screen)).hasBorder(0, 0, 80, 24);
	}

	@Test
	void hasBordersWith2x2() {
		BoxView box1 = new BoxView();
		BoxView box2 = new BoxView();
		BoxView box3 = new BoxView();
		BoxView box4 = new BoxView();

		GridView grid = new GridView();
		grid.borders = true;
		grid.setRowSize(0, 0);
		grid.setColumnSize(0, 0);
		grid.setShowBorder(false);
		grid.addItem(box1, 0, 0, 1, 1, 0, 0);
		grid.addItem(box2, 0, 1, 1, 1, 0, 0);
		grid.addItem(box3, 1, 0, 1, 1, 0, 0);
		grid.addItem(box4, 1, 1, 1, 1, 0, 0);

		grid.setRect(0, 0, 80, 24);
		grid.draw(screen);

		assertThat(forScreen(screen)).hasBorder(0, 0, 80, 24);
	}

	private AssertProvider<ScreenAssert> forScreen(Screen screen) {
		return () -> new ScreenAssert(screen);
	}
}
