package org.springframework.shell.support;

import java.util.stream.Stream;

import org.assertj.core.util.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.shell.support.SearchAlgo.Result;

import static org.assertj.core.api.Assertions.assertThat;

public class SearchAlgoTests {

	// @Test
	// void test1() {
	// 	Result result = SearchAlgo.match(false, false, true, "fooBarbaz1", "obz", false);
	// 	assertThat(result).isNotNull();
	// 	assertThat(result.start).isEqualTo(2);
	// 	assertThat(result.end).isEqualTo(9);
	// 	assertThat(result.score).isEqualTo(SearchAlgo.scoreMatch * 3 + SearchAlgo.bonusCamel123 + SearchAlgo.scoreGapStart + SearchAlgo.scoreGapExtension * 3);
	// }


	private static Stream<Arguments> test3() {
		return Stream.of(
		  Arguments.of(false, false, true, "fooBarbaz1", "obz", false, 2, 9, SearchAlgo.scoreMatch * 3 + SearchAlgo.bonusCamel123 + SearchAlgo.scoreGapStart + SearchAlgo.scoreGapExtension * 3),
		  Arguments.of(true, false, true, "fooBarbaz", "oBz", false, 2, 9, SearchAlgo.scoreMatch * 3 + SearchAlgo.bonusCamel123 + SearchAlgo.scoreGapStart + SearchAlgo.scoreGapExtension * 3)
		);
	}

	@ParameterizedTest
	@MethodSource
	void test3(boolean caseSensitive, boolean normalize, boolean forward, String text, String pattern, boolean withPos, int start, int end, int score) {
		Result result = SearchAlgo.match(caseSensitive, normalize, forward, text, pattern, withPos);
		assertThat(result.start).isEqualTo(start);
		assertThat(result.end).isEqualTo(end);
		assertThat(result.score).isEqualTo(score);
	}

}
