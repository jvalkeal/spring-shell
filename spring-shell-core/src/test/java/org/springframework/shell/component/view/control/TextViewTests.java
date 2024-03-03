/*
 * Copyright 2024 the original author or authors.
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
package org.springframework.shell.component.view.control;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TextViewTests extends AbstractViewTests {

	private String MULTILINE_FEW_SHORT = """
			line1
			line2
			line3
			""";

	private String MULTILINE_MANY_SHORT = """
				line1
				line2
				line3
				line4
				line5
				line6
				line7
				line8
				line9
				""";

	@Nested
	class Construction {

		TextView view;

		@Test
		void constructDefault() {
			view = new TextView();
		}

	}

	@Nested
	class Visual {

		TextView view;

		@BeforeEach
		void setup() {
			view = new TextView();
			view.setRect(0, 0, 10, 7);
			configure(view);
		}

		@Test
		void simpleMultiLine() {
			view.setContent(MULTILINE_FEW_SHORT);
			view.draw(screen7x10);
			assertThat(forScreen(screen7x10)).hasHorizontalText("line1", 0, 0, 5);
			assertThat(forScreen(screen7x10)).hasHorizontalText("line2", 0, 1, 5);
			assertThat(forScreen(screen7x10)).hasHorizontalText("line3", 0, 2, 5);
		}

		@Test
		void moreLinesThanScreenHeight() {
			view.setContent(MULTILINE_MANY_SHORT);
			view.draw(screen7x10);
			assertThat(forScreen(screen7x10)).hasHorizontalText("line1", 0, 0, 5);
			assertThat(forScreen(screen7x10)).hasHorizontalText("line7", 0, 6, 5);
		}

	}

	@Nested
	class Events {

	}

	@Nested
	class Styling {

	}

}
