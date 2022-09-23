package org.springframework.shell.support.search;

class FuzzyMatchV1SearchMatchAlgorithm extends AbstractSearchMatchAlgorithm {

	private int pidx = 0;
	private int sidx = -1;
	private int eidx = -1;

	@Override
	SearchMatchResult match(boolean caseSensitive, boolean normalize, boolean forward, String text, String pattern,
			boolean withPos) {
		int lenRunes = text.length();
		int lenPattern = pattern.length();

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
			CalculateScore calculateScore = calculateScore(caseSensitive, normalize, text, pattern, sidx, eidx,
					withPos);
			return SearchMatchResult.of(sidx, eidx, calculateScore.score, calculateScore.pos);
		}
		return SearchMatchResult.of(-1, -1, 0, new int[0]);
	}
}
