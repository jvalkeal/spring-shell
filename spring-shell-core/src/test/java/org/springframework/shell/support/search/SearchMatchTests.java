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
package org.springframework.shell.support.search;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.shell.support.search.AbstractSearchMatchAlgorithm.BONUS_CAMEL123;
import static org.springframework.shell.support.search.AbstractSearchMatchAlgorithm.BONUS_CONSECUTIVE;
import static org.springframework.shell.support.search.AbstractSearchMatchAlgorithm.SCORE_GAP_EXTENSION;
import static org.springframework.shell.support.search.AbstractSearchMatchAlgorithm.SCORE_GAP_START;
import static org.springframework.shell.support.search.AbstractSearchMatchAlgorithm.SCORE_MATCH;

public class SearchMatchTests {

	static Stream<Arguments> testFuzzyMatch() {
		return Stream.of(
			// Arguments.of(false, false, true, "fooBarbaz1", "obz", false, 2, 9,
			// 	SCORE_MATCH * 3 + BONUS_CAMEL123 + SCORE_GAP_START + SCORE_GAP_EXTENSION * 3),
			Arguments.of(true, false, true, "fooBarbaz", "oBz", false, 2, 9,
				SCORE_MATCH * 3 + BONUS_CAMEL123 + SCORE_GAP_START + SCORE_GAP_EXTENSION * 3)
			);
	}

	@ParameterizedTest
	@MethodSource
	void testFuzzyMatch(boolean caseSensitive, boolean normalize, boolean forward, String text, String pattern, boolean withPos,
			int start, int end, int score) {
		SearchMatch searchMatch = SearchMatch.builder()
				.caseSensitive(caseSensitive)
				.normalize(normalize)
				.forward(forward)
				.build();
		SearchMatchResult result = searchMatch.match(text, pattern);
		assertThat(result.getStart()).isEqualTo(start);
		assertThat(result.getEnd()).isEqualTo(end);
		assertThat(result.getScore()).isEqualTo(score);
	}

	static Stream<Arguments> testExactMatch() {
		return Stream.of(
			Arguments.of(false, false, true, "fooBarbaz", "'oBA", false, 2, 5,
				SCORE_MATCH * 3 + BONUS_CAMEL123 + BONUS_CONSECUTIVE)
			);
	}

	// @ParameterizedTest
	// @MethodSource
	void testExactMatch(boolean caseSensitive, boolean normalize, boolean forward, String text, String pattern,
			boolean withPos, int start, int end, int score) {
		SearchMatch searchMatch = SearchMatch.builder()
				.caseSensitive(caseSensitive)
				.normalize(normalize)
				.forward(forward)
				.build();
		SearchMatchResult result = searchMatch.match(text, pattern);
		assertThat(result.getStart()).isEqualTo(start);
		assertThat(result.getEnd()).isEqualTo(end);
		assertThat(result.getScore()).isEqualTo(score);
	}
}
