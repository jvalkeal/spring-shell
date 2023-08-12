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
package org.springframework.shell.component.view.control;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ButtonViewTests extends AbstractViewTests {

	static final String TEXT_FIELD = "text";
	static final String ACTION_FIELD = "action";
	ButtonView view;

	@Nested
	class Construction {

		@Test
		void defaultView() {
			view = new ButtonView();
			assertThat(getStringField(view, TEXT_FIELD)).isNull();
			assertThat(getRunnableField(view, ACTION_FIELD)).isNull();
		}

		@Test
		void text() {
			view = new ButtonView("text");
			assertThat(getStringField(view, TEXT_FIELD)).isEqualTo("text");
			assertThat(getRunnableField(view, ACTION_FIELD)).isNull();
		}

		@Test
		void textAndAction() {
			view = new ButtonView("text", () -> {});
			assertThat(getStringField(view, TEXT_FIELD)).isEqualTo("text");
			assertThat(getRunnableField(view, ACTION_FIELD)).isNotNull();
		}

		@Test
		void canSetTextAndAction() {
			view = new ButtonView();
			view.setText("text");
			view.setAction(() -> {});
			assertThat(getStringField(view, TEXT_FIELD)).isEqualTo("text");
			assertThat(getRunnableField(view, ACTION_FIELD)).isNotNull();
		}

	}

	@Nested
	class Events {

		@BeforeEach
		void setup() {
			view = new ButtonView();
			configure(view);
		}

		@Test
		void handlesMouseClick() {

		}

	}

	@Nested
	class Visual {

		@BeforeEach
		void setup() {
			view = new ButtonView("text");
		}

	}

}
