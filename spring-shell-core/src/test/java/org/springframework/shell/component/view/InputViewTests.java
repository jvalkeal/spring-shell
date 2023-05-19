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

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InputViewTests extends AbstractViewTests {

	@Test
	void test() {
		InputView view = new InputView();
		view.setShowBorder(true);
		view.setRect(0, 0, 80, 24);

		BiConsumer<KeyEvent, Consumer<View>> inputHandler = view.getInputHandler();
		if (inputHandler != null) {
			KeyEvent event = KeyEvent.ofCharacter("1");
			inputHandler.accept(event, v -> {});
		}

		view.draw(screen24x80);

		assertThat(forScreen(screen24x80)).hasHorizontalText("1", 1, 1, 1);

	}
}
