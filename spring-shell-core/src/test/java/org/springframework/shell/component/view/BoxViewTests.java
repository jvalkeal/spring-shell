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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BoxViewTests extends AbstractViewTests {

	@Test
	void hasBorder() {
		BoxView view = new BoxView();
		view.setShowBorder(true);
		view.setRect(0, 0, 80, 24);
		view.draw(screen24x80);
		assertThat(forScreen(screen24x80)).hasBorder(0, 0, 80, 24);

		BoxView view2 = new BoxView();
		view2.setShowBorder(true);
		view2.setRect(0, 0, 10, 7);
		view2.draw(screen7x10);
		assertThat(forScreen(screen7x10)).hasBorder(0, 0, 10, 7);
	}

	@Test
	void hasNoBorder() {
		BoxView view = new BoxView();
		view.setShowBorder(false);
		view.setRect(0, 0, 80, 24);
		view.draw(screen24x80);
		assertThat(forScreen(screen24x80)).hasNoBorder(0, 0, 80, 24);
	}

	@Test
	void hasTitle() {
		BoxView view = new BoxView();
		view.setShowBorder(true);
		view.setTitle("title");
		view.setRect(0, 0, 80, 24);
		view.draw(screen24x80);
		assertThat(forScreen(screen24x80)).hasHorizontalText("title", 0, 1, 5);
	}

	@Test
	void hasNoTitleWhenNoBorder() {
		BoxView view = new BoxView();
		view.setShowBorder(false);
		view.setTitle("title");
		view.setRect(0, 0, 80, 24);
		view.draw(screen24x80);
		assertThat(forScreen(screen24x80)).hasNoHorizontalText("title", 0, 1, 5);
	}
}
