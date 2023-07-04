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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.shell.component.view.event.KeyEvent.Key;

import static org.assertj.core.api.Assertions.assertThat;

class KeyEventTests {

	// set bit
	// i | (1 << n)
	// get bit
	// (i >> n) & 1

	// 1 << Key.CharMask
	// 80000000

	// 0x80000041
	// ----------
	// 0x00000041


	@Test
	void test1() {

		// int a = Key.A | (1 << Key.CharMask);
		// // 0x80000041
		// String hex = Integer.toHexString(1 << Key.CharMask);
		// // int a1 = a | (1 >> Key.CharMask);
		// a = a | (1 << Key.CtrlMask);
		// a = a | (1 << Key.ShiftMask);
		// a = a | (1 << Key.AltMask);
		// assertThat((a >> Key.CharMask) & 1).isEqualTo(1);
		// assertThat((a >> Key.CtrlMask) & 1).isEqualTo(1);
		// assertThat((a >> Key.ShiftMask) & 1).isEqualTo(1);
		// assertThat((a >> Key.AltMask) & 1).isEqualTo(1);

		// // assertThat(a1).isEqualTo(65);

		// // assertThat((Key.A >> Key.CharMask) & 1).isEqualTo(1);
	}

	@Test
	void test2() {
		// Key.A 	0x00000041
		// A_CTRL	0x40000041
		// A_plain 	0x00000041
		int A_CTRL = Key.A | Key.CtrlMask;
		int A_plain = A_CTRL ^ Key.CtrlMask;
		assertThat((A_CTRL >> Key.CtrlMask) & 1).isEqualTo(1);
		assertThat(A_plain).isEqualTo(65);
	}

	// @Nested
	// static class Keys {

	// 	@Test
	// 	void test() {
	// 		// (i >> n) & 1
	// 		assertThat((Key.A >> Key.CharMask) & 1).isEqualTo(1);
	// 	}
	// }
}
