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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.shell.component.view.BoxView;
import org.springframework.shell.component.view.GridView;
import org.springframework.shell.component.view.Screen;

import static org.assertj.core.api.Assertions.assertThat;

class GridViewTests {

	private Screen display;

	@BeforeEach
	void setup() {
		display = new Screen(24, 80);
	}

	@Test
	void test1() {
		BoxView menu = new BoxView();
		menu.setTitle("Menu");
		menu.setShowBorder(true);

		GridView grid = new GridView();
		grid.borders = true;
		grid.setRowSize(1);
		grid.setColumnSize(1);
		// grid.setShowBorder(true);
		grid.addItem(menu, 0, 0, 1, 1, 0, 0);
		grid.setRect(0, 0, 80, 24);
		grid.draw(display);

		char[][] content = display.getData();
		assertThat(content).isNotNull();
	}

}
