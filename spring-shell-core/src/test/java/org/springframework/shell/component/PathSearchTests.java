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
package org.springframework.shell.component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.shell.component.PathSearch.PathSearchContext;
import org.springframework.shell.component.PathSearch.PathSearchContext.NameMatchPart;

import static org.assertj.core.api.Assertions.assertThat;

public class PathSearchTests {

	static Stream<Arguments> test() {
		return Stream.of(
			Arguments.of("0", new int[] { 0 },
				Arrays.asList(NameMatchPart.of("0", true))),
			Arguments.of("01", new int[] { 0, 1 },
				Arrays.asList(
					NameMatchPart.of("0", true),
					NameMatchPart.of("1", true))),
			Arguments.of("012", new int[] { 0, 1, 2 },
				Arrays.asList(
					NameMatchPart.of("0", true),
					NameMatchPart.of("1", true),
					NameMatchPart.of("2", true))),
			Arguments.of("0123456789", new int[0],
				Arrays.asList(NameMatchPart.of("0123456789", false))),
			Arguments.of("0123456789", new int[] { 0 },
				Arrays.asList(
					NameMatchPart.of("0", true),
					NameMatchPart.of("123456789", false))),
			Arguments.of("0123456789", new int[] { 1 },
				Arrays.asList(
					NameMatchPart.of("0", false),
					NameMatchPart.of("1", true),
					NameMatchPart.of("23456789", false))),
			Arguments.of("0123456789", new int[] { 10 },
				Arrays.asList(
					NameMatchPart.of("012345678", false),
					NameMatchPart.of("9", true))),
			Arguments.of("0123456789", new int[] { 2, 5 },
				Arrays.asList(
					NameMatchPart.of("01", false),
					NameMatchPart.of("2", true),
					NameMatchPart.of("34", false),
					NameMatchPart.of("5", true),
					NameMatchPart.of("6789", false))),
			Arguments.of("0123456789", new int[] { 2, 3 },
				Arrays.asList(
					NameMatchPart.of("01", false),
					NameMatchPart.of("2", true),
					NameMatchPart.of("3", true),
					NameMatchPart.of("456789", false))),
			Arguments.of("0123456789", new int[] { 9, 10 },
				Arrays.asList(
					NameMatchPart.of("012345678", false),
					NameMatchPart.of("9", true),
					NameMatchPart.of("10", true)))
			);
	}

	@ParameterizedTest
	@MethodSource
	void test(String text, int[] positions, List<NameMatchPart> parts) {
		List<NameMatchPart> res = PathSearchContext.ofNameMatchParts2(text, positions);
		assertThat(res).hasSize(parts.size());
		for (int i = 0; i < parts.size(); i++) {
			assertThat(res.get(i).getPart()).isEqualTo(parts.get(i).getPart());
			assertThat(res.get(i).getMatch()).isEqualTo(parts.get(i).getMatch());
		}
	}

}
