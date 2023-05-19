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

import org.assertj.core.api.AssertProvider;
import org.junit.jupiter.api.BeforeEach;

class AbstractViewTests {

	Screen screen24x80;

	@BeforeEach
	void setupScreens() {
		screen24x80 = new Screen(24, 80);
	}

	void dispatchEvent(View view, KeyEvent event) {
		BiConsumer<KeyEvent, Consumer<View>> inputHandler = view.getInputHandler();
		if (inputHandler != null) {
			inputHandler.accept(event, v -> {});
		}
	}

	AssertProvider<ScreenAssert> forScreen(Screen screen) {
		return () -> new ScreenAssert(screen);
	}
}
