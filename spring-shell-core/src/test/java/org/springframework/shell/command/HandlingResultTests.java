/*
 * Copyright 2022 the original author or authors.
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
package org.springframework.shell.command;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HandlingResultTests {

	@Test
	void hasMessageFromOf() {
		HandlingResult result = HandlingResult.of("fake");
		assertThat(result).isNotNull();
		assertThat(result.message()).isEqualTo("fake");
		assertThat(result.isPresent()).isTrue();
		assertThat(result.isEmpty()).isFalse();
	}

	@Test
	void hasNotMessageFromOf() {
		HandlingResult result = HandlingResult.of(null);
		assertThat(result).isNotNull();
		assertThat(result.message()).isNull();
		assertThat(result.isPresent()).isFalse();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	void hasNotMessageFromEmpty() {
		HandlingResult result = HandlingResult.empty();
		assertThat(result).isNotNull();
		assertThat(result.message()).isNull();
		assertThat(result.isPresent()).isFalse();
		assertThat(result.isEmpty()).isTrue();
	}
}
