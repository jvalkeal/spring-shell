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
package org.springframework.shell.component.xxx;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BoxViewTests {

	private VirtualDisplay display;

	@BeforeEach
	void setup() {
		display = new VirtualDisplay(24, 80);
	}

	@Test
	void hasBorder() {
		BoxView view = new BoxView();
		view.setShowBorder(true);
		view.setRect(0, 0, 80, 24);
		view.draw(display);
		char[][] data = display.getData();
		assertThat(data[0][0]).isNotEqualTo(' ');
		assertThat(data[0][79]).isNotEqualTo(' ');
		assertThat(data[23][0]).isNotEqualTo(' ');
		assertThat(data[23][79]).isNotEqualTo(' ');
		assertThat(data[0][1]).isNotEqualTo(' ');
		assertThat(data[1][0]).isNotEqualTo(' ');
	}

	@Test
	void hasNoBorder() {
		BoxView view = new BoxView();
		view.setShowBorder(false);
		view.setRect(0, 0, 80, 24);
		view.draw(display);
		char[][] data = display.getData();
		assertThat(data[0][0]).isEqualTo(' ');
		assertThat(data[0][79]).isEqualTo(' ');
		assertThat(data[23][0]).isEqualTo(' ');
		assertThat(data[23][79]).isEqualTo(' ');
		assertThat(data[0][1]).isEqualTo(' ');
		assertThat(data[1][0]).isEqualTo(' ');
	}

	@Test
	void hasTitle() {
		BoxView view = new BoxView();
		view.setShowBorder(true);
		view.setTitle("title");
		view.setRect(0, 0, 80, 24);
		view.draw(display);
		char[][] data = display.getData();
		String line = new String(data[0]);
		assertThat(line).contains("title");
	}
}
