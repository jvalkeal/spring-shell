package org.springframework.shell.support.search;

abstract class AbstractSearchMatchAlgorithm {

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

	static enum charClass {
		charWhite,
		charNonWord,
		charDelimiter,
		charLower,
		charUpper,
		charLetter,
		charNumber
	}

	static class CalculateScore {
		int score;
		int pos[];

		CalculateScore(int score, int[] pos) {
			this.score = score;
			this.pos = pos;
		}
	}

	abstract SearchMatchResult match(boolean caseSensitive, boolean normalize, boolean forward, String text, String pattern, boolean withPos);

	static int indexAt(int index, int max, boolean forward) {
		if (forward) {
			return index;
		}
		return max - index - 1;
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

	static CalculateScore calculateScore(boolean caseSensitive, boolean normalize, String text, String pattern,
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

			// if (pattern.length() < pidx && c == pattern.charAt(pidx)) {
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
