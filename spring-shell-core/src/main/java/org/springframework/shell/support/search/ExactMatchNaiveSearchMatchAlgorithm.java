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

class ExactMatchNaiveSearchMatchAlgorithm extends AbstractSearchMatchAlgorithm {

	@Override
	public SearchMatchResult match(boolean caseSensitive, boolean normalize, boolean forward, String text,
			String pattern, boolean withPos) {

		if (!StringUtils.hasText(pattern)) {
			return SearchMatchResult.ofZeros();
		}

		int lenRunes = text.length();
		int lenPattern = pattern.length();

		if (lenRunes < lenPattern) {
			return SearchMatchResult.ofMinus();
		}

		if (asciiFuzzyIndex(text, pattern, caseSensitive) < 0) {
			return SearchMatchResult.ofMinus();
		}

			// 	if asciiFuzzyIndex(text, pattern, caseSensitive) < 0 {
			// 		return Result{-1, -1, 0}, nil
			// 	}

			int pidx = 0;
			int bestPos = -1;
			int bonus = 0;
			int bestBonus = -1;

			// 	// For simplicity, only look at the bonus at the first character position
			// 	pidx := 0
			// 	bestPos, bonus, bestBonus := -1, int16(0), int16(-1)

			for (int index = 0; index < lenRunes; index++) {

			// 	for index := 0; index < lenRunes; index++ {

				int index_ = indexAt(index, lenRunes, forward);
				char c = text.charAt(index_);
			// 		index_ := indexAt(index, lenRunes, forward)
			// 		char := text.Get(index_)

				if (!caseSensitive) {
					if (c >= 'A' && c <= 'Z') {
						c += 32;
					}
					if (normalize) {
						// char = normalizeRune(char)
					}
				}
			// 		if !caseSensitive {
			// 			if char >= 'A' && char <= 'Z' {
			// 				char += 32
			// 			} else if char > unicode.MaxASCII {
			// 				char = unicode.To(unicode.LowerCase, char)
			// 			}
			// 		}
			// 		if normalize {
			// 			char = normalizeRune(char)
			// 		}

				int pidx_ = indexAt(pidx, lenPattern, forward);
				char pchar = pattern.charAt(pidx_);
			// 		pidx_ := indexAt(pidx, lenPattern, forward)
			// 		pchar := pattern[pidx_]

				if (c == pchar) {
					if (pidx_ == 0) {
						bonus = bonusAt(text, index_);
					}
					pidx++;
					if (pidx == lenPattern) {
						if (bonus > bestBonus) {
							bestPos = index;
							bestBonus = bonus;
						}
						if (bonus >= BONUS_BOUNDARY) {
							break;
						}
						index -= pidx - 1;
						pidx = 0;
						bonus = 0;
					}
				}
			// 		if pchar == char {
			// 			if pidx_ == 0 {
			// 				bonus = bonusAt(text, index_)
			// 			}
			// 			pidx++
			// 			if pidx == lenPattern {
			// 				if bonus > bestBonus {
			// 					bestPos, bestBonus = index, bonus
			// 				}
			// 				if bonus >= bonusBoundary {
			// 					break
			// 				}
			// 				index -= pidx - 1
			// 				pidx, bonus = 0, 0
			// 			}

				else {
					index -= pidx;
					pidx = 0;
					bonus = 0;
				}
			// 		} else {
			// 			index -= pidx
			// 			pidx, bonus = 0, 0
			// 		}
			// 	}
			}

			if (bestPos >= 0) {
				int sidx;
				int eidx;
				if (forward) {
					sidx = bestPos - lenPattern + 1;
					eidx = bestPos + 1;
				}
				else {
					sidx = lenRunes - (bestPos + 1);
					eidx = lenRunes - (bestPos - lenPattern + 1);
				}
				CalculateScore score = calculateScore(caseSensitive, normalize, text, pattern, sidx, eidx, false);
				return SearchMatchResult.of(sidx, eidx, score.score, new int[0]);
			}
			// 	if bestPos >= 0 {
			// 		var sidx, eidx int
			// 		if forward {
			// 			sidx = bestPos - lenPattern + 1
			// 			eidx = bestPos + 1
			// 		} else {
			// 			sidx = lenRunes - (bestPos + 1)
			// 			eidx = lenRunes - (bestPos - lenPattern + 1)
			// 		}
			// 		score, _ := calculateScore(caseSensitive, normalize, text, pattern, sidx, eidx, false)
			// 		return Result{sidx, eidx, score}, nil
			// 	}
			// 	return Result{-1, -1, 0}, nil
		return SearchMatchResult.ofMinus();
	}
}
