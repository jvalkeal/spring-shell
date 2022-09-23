package org.springframework.shell.support;

public class SearchAlgo {

	public static Result match(boolean caseSensitive, boolean normalize, boolean forward, String text, String pattern, boolean withPos) {
		FuzzyMatchV1 fuzzyMatchV1 = new FuzzyMatchV1();
		return fuzzyMatchV1.match(caseSensitive, normalize, forward, text, pattern, withPos);
	}

	private static int indexAt(int index, int max, boolean forward) {
		if (forward) {
			return index;
		}
		return max - index - 1;
	}

	static class FuzzyMatchV1 {

		private int pidx = 0;
		private int sidx = -1;
		private int eidx = -1;

		public Result match(boolean caseSensitive, boolean normalize, boolean forward, String text, String pattern, boolean withPos) {
			int lenRunes = text.length();
			int lenPattern = pattern.length();

			// if (!caseSensitive) {
			// 	text = text.toLowerCase();
			// 	pattern = pattern.toLowerCase();
			// }

			for (int index = 0; index < lenRunes; index++) {
				char c = text.charAt(indexAt(index, lenRunes, forward));

				if (!caseSensitive) {
					if (c >= 'A' && c <= 'Z') {
						c += 32;
					}
				}

				if (normalize) {
					// c = normalizeRune(c);
				}

				char pchar = pattern.charAt(indexAt(pidx, lenPattern, forward));

				if (c == pchar) {
					if (sidx < 0) {
						sidx = index;
					}

					pidx++;
					if (pidx == lenPattern) {
						eidx = index + 1;
						break;
					}
				}

			}

			if (sidx >= 0 && eidx >= 0) {
				pidx--;
				for (int i = eidx - 1; i >= sidx; i--) {
					int tidx = indexAt(i, lenRunes, forward);
					char c = text.charAt(tidx);
					if (!caseSensitive) {
						if (c >= 'A' && c <= 'Z') {
							c += 32;
						}
					}
					int pidx_ = indexAt(pidx, lenPattern, forward);
					char pchar = pattern.charAt(pidx_);
					if (c == pchar) {
						pidx--;
						if (pidx < 0) {
							sidx = i;
							break;
						}
					}
				}
				if (!forward) {
					sidx = lenRunes - eidx;
					eidx = lenRunes - sidx;
				}
				CalculateScore calculateScore = calculateScore(caseSensitive, normalize, text, pattern, sidx, eidx, withPos);
				return new Result(sidx, eidx, calculateScore.score, calculateScore.pos);
			}

			return new Result(-1, -1, 0, new int[0]);
		}


	}

	public static class Result {

		int start;
		int end;
		int score;
		int[] pos;
		public Result(int start, int end, int score, int[] pos) {
			this.start = start;
			this.end = end;
			this.score = score;
			this.pos = pos;
		}

		public int getScore() {
			return score;
		}
	}

	private static class CalculateScore {
		int score;
		int pos[];
		public CalculateScore(int score, int[] pos) {
			this.score = score;
			this.pos = pos;
		}


	}

	public static enum charClass {
		charWhite,
		charNonWord,
		charDelimiter,
		charLower,
		charUpper,
		charLetter,
		charNumber
	}

	public static final int scoreMatch = 16;
	public static final int scoreGapStart = -3;
	public static final int scoreGapExtension = -1;
	public static final int bonusBoundary = scoreMatch / 2;
	public static final int bonusNonWord = scoreMatch / 2;
	public static final int bonusCamel123 = bonusBoundary + scoreGapExtension;
	public static final int bonusConsecutive = -(scoreGapStart + scoreGapExtension);
	public static final int bonusFirstCharMultiplier = 2;
	public static final int bonusBoundaryWhite = bonusBoundary;
	public static final int bonusBoundaryDelimiter = bonusBoundary + 1;


	private static int bonusFor(charClass prevClass, charClass clazz) {
		if (clazz.ordinal() > charClass.charNonWord.ordinal()) {
			if (prevClass == charClass.charWhite) {
				// Word boundary after whitespace
				return bonusBoundaryWhite;
			} else if (prevClass == charClass.charDelimiter) {
				// Word boundary after a delimiter character
				return bonusBoundaryDelimiter;
			} else if (prevClass == charClass.charNonWord) {
				// Word boundary
				return bonusBoundary;
			}
		}
		if (prevClass == charClass.charLower && clazz == charClass.charUpper ||
			prevClass != charClass.charNumber && clazz == charClass.charNumber) {
			// camelCase letter123
			return bonusCamel123;
		} else if (clazz == charClass.charNonWord) {
			return bonusNonWord;
		} else if (clazz == charClass.charWhite) {
			return bonusBoundaryWhite;
		}
		return 0;
	}

	private static charClass charClassOfAscii(char c) {
		if (c >= 'a' && c <= 'z') {
			return charClass.charLower;
		}
		else if (c >= 'A' && c <= 'Z') {
			return charClass.charUpper;
		}
		else if (c >= '0' && c <= '9') {
			return charClass.charNumber;
		}

		// Character.isWhitespace(arg0)
		else if (Character.isWhitespace(c)) {
			return charClass.charWhite;
		}

		// else if strings.IndexRune(whiteChars, char) >= 0 {
		// 	return charWhite
		// }


		// else if strings.IndexRune(delimiterChars, char) >= 0 {
		// 	return charDelimiter
		// }
		return charClass.charNonWord;
	}


	private static CalculateScore calculateScore(boolean caseSensitive, boolean normalize, String text, String pattern,
			int sidx, int eidx, boolean withPos) {
		int pidx = 0;
		int score = 0;
		boolean inGap = false;
		int consecutive = 0;
		int firstBonus = 0;
		int[] pos = withPos ? new int[pattern.length()] : new int[0];

		charClass prevClass = charClass.charWhite;
		if (sidx > 0) {
			prevClass = charClassOfAscii(text.charAt(sidx - 1));
		}

		for (int idx = sidx; idx < eidx; idx++) {
			char c = text.charAt(idx);
			charClass clazz = charClassOfAscii(c);

			if (!caseSensitive) {
				if (c >= 'A' && c <= 'Z') {
					c += 32;
				}
			}

			if (normalize) {
				// c = normilizeRune(c);
			}

			if (c == pattern.charAt(pidx)) {
				if (withPos) {
					// *pos = append(*pos, idx)
				}
				score += scoreMatch;
				int bonus = bonusFor(prevClass, clazz);
				if (consecutive == 0) {
					firstBonus = bonus;
				}
				else {
					if (bonus >= bonusBoundary && bonus > firstBonus) {
						firstBonus = bonus;
					}
					bonus = Math.max(Math.max(bonus, firstBonus), bonusConsecutive);
				}
				if (pidx == 0) {
					score += bonus * bonusFirstCharMultiplier;
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
					score += scoreGapExtension;
				}
				else {
					score += scoreGapStart;
				}
				inGap = true;
				consecutive = 0;
				firstBonus = 0;
			}
			prevClass = clazz;

		}

		return new CalculateScore(score, pos);
	}


}
