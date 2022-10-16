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
import static org.springframework.shell.support.search.AbstractSearchMatchAlgorithm.BONUS_BOUNDARY;
import static org.springframework.shell.support.search.AbstractSearchMatchAlgorithm.BONUS_BOUNDARY_DELIMITER;
import static org.springframework.shell.support.search.AbstractSearchMatchAlgorithm.BONUS_BOUNDARY_WHITE;
import static org.springframework.shell.support.search.AbstractSearchMatchAlgorithm.BONUS_CAMEL123;
import static org.springframework.shell.support.search.AbstractSearchMatchAlgorithm.BONUS_CONSECUTIVE;
import static org.springframework.shell.support.search.AbstractSearchMatchAlgorithm.BONUS_FIRST_CHAR_MULTIPLIER;
import static org.springframework.shell.support.search.AbstractSearchMatchAlgorithm.BONUS_NON_WORD;
import static org.springframework.shell.support.search.AbstractSearchMatchAlgorithm.SCORE_GAP_EXTENSION;
import static org.springframework.shell.support.search.AbstractSearchMatchAlgorithm.SCORE_GAP_START;
import static org.springframework.shell.support.search.AbstractSearchMatchAlgorithm.SCORE_MATCH;

public class SearchMatchTests {

	static Stream<Arguments> testv2() {
		return Stream.of(
			Arguments.of(false, false, true, "fooBarbaz1", "oBZ", true, 2, 9, new int[] { 2, 3, 8 },
				SCORE_MATCH * 3 + BONUS_CAMEL123 + SCORE_GAP_START + SCORE_GAP_EXTENSION * 3),
			Arguments.of(false, false, true, "foo bar baz", "fbb", true, 0, 9, new int[] { 0, 4, 8 },
				SCORE_MATCH * 3 + BONUS_BOUNDARY_WHITE * BONUS_FIRST_CHAR_MULTIPLIER + BONUS_BOUNDARY_WHITE * 2 +
				2 * SCORE_GAP_START + 4 * SCORE_GAP_EXTENSION),
			Arguments.of(false, false, true, "/AutomatorDocument.icns", "rdoc", true, 9, 13, new int[] { 9, 10, 11, 12 },
				SCORE_MATCH * 4 + BONUS_CAMEL123 + BONUS_CONSECUTIVE * 2),
			Arguments.of(false, false, true, "/man1/zshcompctl.1", "zshc", true, 6, 10, new int[] { 6, 7, 8, 9 },
				SCORE_MATCH * 4 + BONUS_BOUNDARY_DELIMITER * BONUS_FIRST_CHAR_MULTIPLIER + BONUS_BOUNDARY_DELIMITER * 3),
			Arguments.of(false, false, true, "/.oh-my-zsh/cache", "zshc", true, 8, 13, new int[] { 8, 9, 10 ,12 },
				SCORE_MATCH * 4 + BONUS_BOUNDARY * BONUS_FIRST_CHAR_MULTIPLIER + BONUS_BOUNDARY * 2 + SCORE_GAP_START + BONUS_BOUNDARY_DELIMITER),
			Arguments.of(false, false, true, "ab0123 456", "12356", true, 3, 10, new int[] { 3, 4, 5, 8, 9 },
				SCORE_MATCH * 5 + BONUS_CONSECUTIVE * 3 + SCORE_GAP_START + SCORE_GAP_EXTENSION),
			Arguments.of(false, false, true, "abc123 456", "12356", true, 3, 10, new int[] { 3, 4, 5, 8, 9 },
				SCORE_MATCH * 5 + BONUS_CAMEL123 * BONUS_FIRST_CHAR_MULTIPLIER + BONUS_CAMEL123 * 2 + BONUS_CONSECUTIVE + SCORE_GAP_START + SCORE_GAP_EXTENSION),
			Arguments.of(false, false, true, "foo/bar/baz", "fbb", true, 0, 9, new int[] { 0, 4, 8 },
				SCORE_MATCH * 3 + BONUS_BOUNDARY_WHITE * BONUS_FIRST_CHAR_MULTIPLIER + BONUS_BOUNDARY_DELIMITER * 2 + SCORE_GAP_START * 2 + SCORE_GAP_EXTENSION * 4),
			Arguments.of(false, false, true, "fooBarBaz", "fbb", true, 0, 7, new int[] { 0, 3, 6 },
				SCORE_MATCH * 3 + BONUS_BOUNDARY_WHITE * BONUS_FIRST_CHAR_MULTIPLIER + BONUS_CAMEL123 * 2 + SCORE_GAP_START * 2 + SCORE_GAP_EXTENSION * 2),
			Arguments.of(false, false, true, "foo barbaz", "fbb", true, 0, 8, new int[] { 0, 4, 7 },
				SCORE_MATCH * 3 + BONUS_BOUNDARY_WHITE * BONUS_FIRST_CHAR_MULTIPLIER + BONUS_BOUNDARY_WHITE +SCORE_GAP_START * 2 + SCORE_GAP_EXTENSION * 3),
			Arguments.of(false, false, true, "fooBar Baz", "foob", true, 0, 4, new int[] { 0, 1, 2, 3 },
				SCORE_MATCH * 4 + BONUS_BOUNDARY_WHITE * BONUS_FIRST_CHAR_MULTIPLIER + BONUS_BOUNDARY_WHITE * 3),
			Arguments.of(false, false, true, "xFoo-Bar Baz", "foo-b", true, 1, 6, new int[] { 1, 2, 3, 4, 5 },
				SCORE_MATCH * 5 + BONUS_CAMEL123 * BONUS_FIRST_CHAR_MULTIPLIER + BONUS_CAMEL123 * 2 + BONUS_NON_WORD + BONUS_BOUNDARY),

			Arguments.of(true, false, true, "fooBarbaz", "oBz", true, 2, 9, new int[] { 2, 3, 8 },
				SCORE_MATCH * 3 + BONUS_CAMEL123 + SCORE_GAP_START + SCORE_GAP_EXTENSION * 3)
			);
	}

	@ParameterizedTest
	@MethodSource
	void testv2(boolean caseSensitive, boolean normalize, boolean forward, String text, String pattern, boolean withPos,
			int start, int end, int[] positions, int score) {
		if (!caseSensitive) {
			pattern = pattern.toLowerCase();
		}
		FuzzyMatchV2SearchMatchAlgorithm algo = new FuzzyMatchV2SearchMatchAlgorithm();
		SearchMatchResult match = algo.match(caseSensitive, normalize, forward, text, pattern, withPos);
		assertThat(match.getStart()).isEqualTo(start);
		assertThat(match.getEnd()).isEqualTo(end);
		assertThat(match.getScore()).isEqualTo(score);
		assertThat(match.getPositions()).containsExactly(positions);
	}

	static Stream<Arguments> testFuzzyMatch() {
		return Stream.of(
			Arguments.of(false, false, true, "fooBarbaz1", "oBZ", false, 2, 9, new int[] { 2, 3, 8 },
				SCORE_MATCH * 3 + BONUS_CAMEL123 + SCORE_GAP_START + SCORE_GAP_EXTENSION * 3),
			Arguments.of(true, false, true, "fooBarbaz", "oBz", false, 2, 9, new int[] { 2, 3, 8 },
				SCORE_MATCH * 3 + BONUS_CAMEL123 + SCORE_GAP_START + SCORE_GAP_EXTENSION * 3)
			);
	}


	@ParameterizedTest
	@MethodSource
	void testFuzzyMatch(boolean caseSensitive, boolean normalize, boolean forward, String text, String pattern, boolean withPos,
			int start, int end, int[] positions, int score) {
		SearchMatch searchMatch = SearchMatch.builder()
				.caseSensitive(caseSensitive)
				.normalize(normalize)
				.forward(forward)
				.build();
		SearchMatchResult result = searchMatch.match(text, pattern);
		assertThat(result.getStart()).isEqualTo(start);
		assertThat(result.getEnd()).isEqualTo(end);
		assertThat(result.getScore()).isEqualTo(score);
		assertThat(result.getPositions()).containsExactly(positions);
	}

	static Stream<Arguments> testExactMatch() {
		return Stream.of(
			Arguments.of(true, false, true, "fooBarbaz", "'oBA", false, -1, -1, new int[] {},
				0),
			Arguments.of(false, false, true, "fooBarbaz", "'oBA", false, 2, 5, new int[] { 2, 3, 4 },
				SCORE_MATCH * 3 + BONUS_CAMEL123 + BONUS_CONSECUTIVE),
			Arguments.of(false, false, true, "fooBarbaz", "'o", false, 1, 2, new int[] { 1 },
				SCORE_MATCH * 1),
			Arguments.of(false, false, true, "/tmp/test/11/file11.txt", "'e", false, 6, 7, new int[] { 6 },
				SCORE_MATCH * 1)
			);
	}

	@ParameterizedTest
	@MethodSource
	void testExactMatch(boolean caseSensitive, boolean normalize, boolean forward, String text, String pattern,
			boolean withPos, int start, int end, int[] positions, int score) {
		SearchMatch searchMatch = SearchMatch.builder()
				.caseSensitive(caseSensitive)
				.normalize(normalize)
				.forward(forward)
				.build();
		SearchMatchResult result = searchMatch.match(text, pattern);
		assertThat(result.getStart()).isEqualTo(start);
		assertThat(result.getEnd()).isEqualTo(end);
		assertThat(result.getScore()).isEqualTo(score);
		assertThat(result.getPositions()).containsExactly(positions);
	}
}
