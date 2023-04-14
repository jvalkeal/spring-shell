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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class VirtualDisplayTests {

	@Test
	void zeroDataSizeDoesntBreak() {
		VirtualDisplay display = new VirtualDisplay();
		assertThat(display.getData()).isEmpty();
		assertThat(display.getScreenLines()).isEmpty();
	}

	@Test
	void cantResizeNegative() {
		VirtualDisplay display = new VirtualDisplay();
		assertThatThrownBy(() -> {
			display.resize(-1, 0);
		}).hasMessageContaining("negative rows");
		assertThatThrownBy(() -> {
			display.resize(0, -1);
		}).hasMessageContaining("negative columns");
	}

	@Test
	void printInBoxShows() {
		VirtualDisplay display = new VirtualDisplay(1, 10);
		display.print("test", 0, 0, 4);
		char[][] data = display.getData();
		String line = new String(data[0]);
		assertThat(line).contains("test");
	}
}
