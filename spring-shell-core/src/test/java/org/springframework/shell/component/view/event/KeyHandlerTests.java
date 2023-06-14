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
package org.springframework.shell.component.view.event;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KeyHandlerTests {

	private static final KeyEvent EVENT = KeyEvent.ofCharacter("x");

	@Test
	void handlesThisIfOtherConsumes() {
		TestKeyHandler h1 = new TestKeyHandler(false);
		TestKeyHandler h2 = new TestKeyHandler(true);

		KeyHandler h = h2.fromIfConsumed(h1);
		h.handle(KeyHandler.argsOf(EVENT));
		assertThat(h1.calls).isEqualTo(1);
		assertThat(h2.calls).isEqualTo(1);
	}

	private static class TestKeyHandler implements KeyHandler {

		boolean willConsume;
		int calls;

		TestKeyHandler(boolean willConsume) {
			this.willConsume = willConsume;
		}

		@Override
		public KeyHandlerResult handle(KeyHandlerArgs args) {
			calls++;
			return KeyHandler.resultOf(args.event(), willConsume, null);
		}

	}
}
