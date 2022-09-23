package org.springframework.shell.support.search;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

public class SearchMatchTests {

	static Stream<Arguments> test() {
		return Stream.of(
		  Arguments.of(false, false, true, "fooBarbaz1", "obz", false, 2, 9, AbstractSearchMatchAlgorithm.scoreMatch * 3 + AbstractSearchMatchAlgorithm.bonusCamel123 + AbstractSearchMatchAlgorithm.scoreGapStart + AbstractSearchMatchAlgorithm.scoreGapExtension * 3),
		  Arguments.of(true, false, true, "fooBarbaz", "oBz", false, 2, 9, AbstractSearchMatchAlgorithm.scoreMatch * 3 + AbstractSearchMatchAlgorithm.bonusCamel123 + AbstractSearchMatchAlgorithm.scoreGapStart + AbstractSearchMatchAlgorithm.scoreGapExtension * 3)
		);
	}

	@ParameterizedTest
	@MethodSource
	void test(boolean caseSensitive, boolean normalize, boolean forward, String text, String pattern, boolean withPos, int start, int end, int score) {
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
