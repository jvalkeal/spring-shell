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

import org.springframework.shell.component.view.event.KeyEvent.Key;
import org.springframework.shell.component.view.event.KeyEvent.KeyMask;

import static org.assertj.core.api.Assertions.assertThat;

class KeyEventTests {

	@Test
	void hasCtrl() {
		assertThat(KeyEvent.of(Key.A).hasCtrl()).isFalse();
		assertThat(KeyEvent.of(Key.A | KeyMask.CtrlMask).hasCtrl()).isTrue();
	}

	@Test
	void plainKey() {
		assertThat(KeyEvent.of(Key.A).getPlainKey()).isEqualTo(Key.A);
		assertThat(KeyEvent.of(Key.A | KeyMask.CtrlMask).getPlainKey()).isEqualTo(Key.A);
	}

	@Test
	void isKey() {
		assertThat(KeyEvent.of(Key.A).isKey(Key.A)).isTrue();
		assertThat(KeyEvent.of(Key.A | KeyMask.CtrlMask).isKey(Key.A)).isTrue();
		assertThat(KeyEvent.of(Key.CursorDown).isKey(Key.CursorDown)).isTrue();
		assertThat(KeyEvent.of(Key.CursorLeft).isKey(Key.CursorRight)).isFalse();

	}

	@Test
	void isKey2() {
		assertThat(KeyEvent.of(Key.A).isKey()).isTrue();
		assertThat(KeyEvent.of(Key.Backspace).isKey()).isFalse();


	}

	// @Test
	// void xxx1() {
	// 	String u1 = "\u1F602";
	// 	String u2 = "😂";
	// 	System.out.println(u1);
	// 	System.out.println(u2);
	// 	assertThat(u1).isNotNull();

	// 	String h1 = "H";
	// 	String h2 = "\u0048";
	// 	System.out.println(h1);
	// 	System.out.println(h2);

	// 	// System.out.println( "\\u" + Integer.toHexString('÷' | 0x10000).substring(1) );
	// }

	// @Test
	// void xxx2() {
	// 	String u1 = "\uD83D\uDE00";
	// 	String u2 = "😀";
	// 	// String xxx = "\u1F600";
	// 	System.out.println(u1);
	// 	System.out.println(u2);
	// 	assertThat(u2).hasSize(2);
	// 	// 60 -40 0 -34
	// }

	// @Test
	// void xxx3() throws UnsupportedEncodingException {
	// 	String u1 = "\uD83D\uDE00";
	// 	String u2 = "😀";
	// 	byte[] b1 = u1.getBytes();
	// 	byte[] bb = u2.getBytes(StandardCharsets.UTF_16LE);
	// 	byte[] b3 = new byte[]{60, -40, 0, -34};
	// 	int emoji = Character.codePointAt(u2, 0);
	// 	// 128512
	// 	assertThat(b3).isNotNull();
	// }

}
