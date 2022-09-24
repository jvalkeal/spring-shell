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

import org.springframework.util.StringUtils;

/**
 * Base class for common search algorithms mostly based on {@code fzf}
 * algorithms.
 *
 * @author Janne Valkealahti
 */
abstract class AbstractSearchMatchAlgorithm implements SearchMatchAlgorithm {

	public static final int SCORE_MATCH = 16;
	public static final int SCORE_GAP_START = -3;
	public static final int SCORE_GAP_EXTENSION = -1;
	public static final int BONUS_BOUNDARY = SCORE_MATCH / 2;
	public static final int BONUS_NON_WORD = SCORE_MATCH / 2;
	public static final int BONUS_CAMEL123 = BONUS_BOUNDARY + SCORE_GAP_EXTENSION;
	public static final int BONUS_CONSECUTIVE = -(SCORE_GAP_START + SCORE_GAP_EXTENSION);
	public static final int BONUS_FIRST_CHAR_MULTIPLIER = 2;
	public static final int BONUS_BOUNDARY_WHITE = BONUS_BOUNDARY;
	public static final int BONUS_BOUNDARY_DELIMITER = BONUS_BOUNDARY + 1;

	static enum CharClass {
		WHITE,
		NONWORD,
		DELIMITER,
		CHARLOWER,
		UPPER,
		LETTER,
		NUMBER
	}

	static class CalculateScore {
		int score;
		int pos[];

		CalculateScore(int score, int[] pos) {
			this.score = score;
			this.pos = pos;
		}
	}

	static int indexAt(int index, int max, boolean forward) {
		if (forward) {
			return index;
		}
		return max - index - 1;
	}

	private static CharClass charClassOfAscii(char c) {
		if (c >= 'a' && c <= 'z') {
			return CharClass.CHARLOWER;
		}
		else if (c >= 'A' && c <= 'Z') {
			return CharClass.UPPER;
		}
		else if (c >= '0' && c <= '9') {
			return CharClass.NUMBER;
		}
		else if (Character.isWhitespace(c)) {
			return CharClass.WHITE;
		}
		// TODO: os and other delimited checks
		// else if strings.IndexRune(delimiterChars, char) >= 0 {
		// 	return charDelimiter
		// }
		return CharClass.NONWORD;
	}

	private static int bonusFor(CharClass prevClass, CharClass clazz) {
		if (clazz.ordinal() > CharClass.NONWORD.ordinal()) {
			if (prevClass == CharClass.WHITE) {
				return BONUS_BOUNDARY_WHITE;
			}
			else if (prevClass == CharClass.DELIMITER) {
				return BONUS_BOUNDARY_DELIMITER;
			}
			else if (prevClass == CharClass.NONWORD) {
				return BONUS_BOUNDARY;
			}
		}
		if (prevClass == CharClass.CHARLOWER && clazz == CharClass.UPPER ||
				prevClass != CharClass.NUMBER && clazz == CharClass.NUMBER) {
			return BONUS_CAMEL123;
		}
		else if (clazz == CharClass.NONWORD) {
			return BONUS_NON_WORD;
		}
		else if (clazz == CharClass.WHITE) {
			return BONUS_BOUNDARY_WHITE;
		}
		return 0;
	}

	static int bonusAt(String input, int idx) {
		if (idx == 0) {
			return BONUS_BOUNDARY_WHITE;
		}
		return bonusFor(CharClass.values()[idx - 1], CharClass.values()[idx]);
		// return bonusFor(charClassOf(input.Get(idx-1)), charClassOf(input.Get(idx)))
	}

	static CalculateScore calculateScore(boolean caseSensitive, boolean normalize, String text, String pattern,
			int sidx, int eidx, boolean withPos) {
		int pidx = 0;
		int score = 0;
		boolean inGap = false;
		int consecutive = 0;
		int firstBonus = 0;
		int[] pos = withPos ? new int[pattern.length()] : new int[0];

		CharClass prevClass = CharClass.WHITE;
		if (sidx > 0) {
			prevClass = charClassOfAscii(text.charAt(sidx - 1));
		}

		for (int idx = sidx; idx < eidx; idx++) {
			char c = text.charAt(idx);
			CharClass clazz = charClassOfAscii(c);

			if (!caseSensitive) {
				if (c >= 'A' && c <= 'Z') {
					c += 32;
				}
			}

			if (normalize) {
				// c = normilizeRune(c);
			}

			// if (pattern.length() < pidx && c == pattern.charAt(pidx)) {
			if (c == pattern.charAt(pidx)) {
				if (withPos) {
					// *pos = append(*pos, idx)
				}
				score += SCORE_MATCH;
				int bonus = bonusFor(prevClass, clazz);
				if (consecutive == 0) {
					firstBonus = bonus;
				}
				else {
					if (bonus >= BONUS_BOUNDARY && bonus > firstBonus) {
						firstBonus = bonus;
					}
					bonus = Math.max(Math.max(bonus, firstBonus), BONUS_CONSECUTIVE);
				}
				if (pidx == 0) {
					score += bonus * BONUS_FIRST_CHAR_MULTIPLIER;
				}
				else {
					score += bonus;
				}
				inGap = false;
				consecutive++;
				pidx++;
			}
			else {
				if (inGap) {
					score += SCORE_GAP_EXTENSION;
				}
				else {
					score += SCORE_GAP_START;
				}
				inGap = true;
				consecutive = 0;
				firstBonus = 0;
			}
			prevClass = clazz;

		}
		return new CalculateScore(score, pos);
	}

	static int trySkip(String input, boolean caseSensitive, char b, int from) {
		String byteArray = input.substring(from);
		int idx = byteArray.indexOf(b);
		// idx := bytes.IndexByte(byteArray, b)

		if (idx == 0) {
			return from;
		}
		// if idx == 0 {
		// 	// Can't skip any further
		// 	return from
		// }
		// // We may need to search for the uppercase letter again. We don't have to
		// // consider normalization as we can be sure that this is an ASCII string.

		if (!caseSensitive && b >= 'a' && b <= 'z') {
			if (idx > 0) {
				byteArray = byteArray.substring(idx);
			}
			int uidx = byteArray.indexOf(b - 32);
			if (uidx >= 0) {
				idx = uidx;
			}
		}
		if (idx < 0) {
			return -1;
		}
		return from + idx;
		// if !caseSensitive && b >= 'a' && b <= 'z' {
		// 	if idx > 0 {
		// 		byteArray = byteArray[:idx]
		// 	}
		// 	uidx := bytes.IndexByte(byteArray, b-32)
		// 	if uidx >= 0 {
		// 		idx = uidx
		// 	}
		// }
		// if idx < 0 {
		// 	return -1
		// }
		// return from + idx

	}

	static int asciiFuzzyIndex(String input, String pattern, boolean caseSensitive) {

		if (!StringUtils.hasText(input)) {
			return 0;
		}
	// 	// Can't determine
	// 	if !input.IsBytes() {
	// 		return 0
	// 	}

	// 	// Not possible
	// 	if !isAscii(pattern) {
	// 		return -1
	// 	}

		int firstIndex = 0;
		int idx = 0;
	// 	firstIdx, idx := 0, 0
		for (int pidx = 0; pidx < pattern.length(); pidx++) {
			idx = trySkip(input, caseSensitive, pattern.charAt(pidx), idx);
			if (idx < 0) {
				return -1;
			}
			if (pidx == 0 && idx > 0) {
				firstIndex = idx - 1;
			}
			idx++;
		}
	// 	for pidx := 0; pidx < len(pattern); pidx++ {
	// 		idx = trySkip(input, caseSensitive, byte(pattern[pidx]), idx)
	// 		if idx < 0 {
	// 			return -1
	// 		}
	// 		if pidx == 0 && idx > 0 {
	// 			// Step back to find the right bonus point
	// 			firstIdx = idx - 1
	// 		}
	// 		idx++
	// 	}
		return firstIndex;
	// 	return firstIdx
	// }
	}

}
