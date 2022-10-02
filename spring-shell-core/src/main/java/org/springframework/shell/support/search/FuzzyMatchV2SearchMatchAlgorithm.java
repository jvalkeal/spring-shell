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
 * Port of {@code fzf} {@code FuzzyMatchV2} algorithm.
 *
 * @author Janne Valkealahti
 */
class FuzzyMatchV2SearchMatchAlgorithm extends AbstractSearchMatchAlgorithm {

	@Override
	public SearchMatchResult match(boolean caseSensitive, boolean normalize, boolean forward, String text, String pattern,
			boolean withPos) {

				// func FuzzyMatchV2(caseSensitive bool, normalize bool, forward bool, input *util.Chars, pattern []rune, withPos bool, slab *util.Slab) (Result, *[]int) {
				// 	// Assume that pattern is given in lowercase if case-insensitive.
				// 	// First check if there's a match and calculate bonus for each position.
				// 	// If the input string is too long, consider finding the matching chars in
				// 	// this phase as well (non-optimal alignment).

				// 	M := len(pattern)
				// 	if M == 0 {
				// 		return Result{0, 0, 0}, posArray(withPos, M)
				// 	}
		if (!StringUtils.hasText(pattern)) {
			return SearchMatchResult.ofZeros();
		}
		int M = pattern.length();

				// 	N := input.Length()
		int N = text.length();

				// 	// Since O(nm) algorithm can be prohibitively expensive for large input,
				// 	// we fall back to the greedy algorithm.
				// 	if slab != nil && N*M > cap(slab.I16) {
				// 		return FuzzyMatchV1(caseSensitive, normalize, forward, input, pattern, withPos, slab)
				// 	}

				// 	// Phase 1. Optimized search for ASCII string
				// 	idx := asciiFuzzyIndex(input, pattern, caseSensitive)
				// 	if idx < 0 {
				// 		return Result{-1, -1, 0}, nil
				// 	}
		int idx = 0;

				// 	// Reuse pre-allocated integer slice to avoid unnecessary sweeping of garbages
				// 	offset16 := 0
				// 	offset32 := 0
				// 	offset16, H0 := alloc16(offset16, slab, N)
				// 	offset16, C0 := alloc16(offset16, slab, N)
		int[] H0 = new int[N];
		int[] C0 = new int[N];

				// 	// Bonus point for each position
				// 	offset16, B := alloc16(offset16, slab, N)
				// 	// The first occurrence of each character in the pattern
				// 	offset32, F := alloc32(offset32, slab, M)
		int[] B = new int[N];
		int[] F = new int[N];

				// 	// Rune array
				// 	_, T := alloc32(offset32, slab, N)
				// 	input.CopyRunes(T)
		String T = text;

				// 	// Phase 2. Calculate bonus for each point
				// 	maxScore, maxScorePos := int16(0), 0
				// 	pidx, lastIdx := 0, 0
		int maxScore = 0;
		int maxScorePos = 0;
		int pidx = 0;
		int lastIdx = 0;

				// 	pchar0, pchar, prevH0, prevClass, inGap := pattern[0], pattern[0], int16(0), initialCharClass, false
		char pchar0 = pattern.charAt(0);
		char pchar = pattern.charAt(0);
		int prevH0 = 0;
		CharClass prevClass = CharClass.DELIMITER;
		boolean inGap = false;

				// 	Tsub := T[idx:]
		String Tsub = T.substring(idx);

				// 	H0sub, C0sub, Bsub := H0[idx:][:len(Tsub)], C0[idx:][:len(Tsub)], B[idx:][:len(Tsub)]
		int[] H0sub = new int[H0.length - idx - (H0.length - idx - Tsub.length())];
		int[] C0sub = new int[C0.length - idx - (C0.length - idx - Tsub.length())];
		int[] Bsub = new int[B.length - idx - (B.length - idx - Tsub.length())];

				// 	for off, char := range Tsub {
		for (int off = 0; off < Tsub.length(); off++) {
			char c = Tsub.charAt(off);

				// 		var class charClass
			CharClass clazz;
				// 		if char <= unicode.MaxASCII {
				// 			class = charClassOfAscii(char)
				// 			if !caseSensitive && class == charUpper {
				// 				char += 32
				// 			}
				// 		} else {
				// 			class = charClassOfNonAscii(char)
				// 			if !caseSensitive && class == charUpper {
				// 				char = unicode.To(unicode.LowerCase, char)
				// 			}
				// 			if normalize {
				// 				char = normalizeRune(char)
				// 			}
				// 		}
			clazz = charClassOfAscii(c);
			if (!caseSensitive && clazz == CharClass.UPPER) {
				if (c >= 'A' && c <= 'Z') {
					c += 32;
				}
			}

				// 		Tsub[off] = char
				// 		bonus := bonusFor(prevClass, class)
				// 		Bsub[off] = bonus
				// 		prevClass = class
				Tsub = Tsub.substring(0, off) + c + Tsub.substring(off +1 );
				int bonus = bonusFor(prevClass, clazz);
				Bsub[off] = bonus;
				prevClass = clazz;

				// 		if char == pchar {
				// 			if pidx < M {
				// 				F[pidx] = int32(idx + off)
				// 				pidx++
				// 				pchar = pattern[util.Min(pidx, M-1)]
				// 			}
				// 			lastIdx = idx + off
				// 		}
				if (c == pchar) {
					if (pidx < M) {
						F[pidx] = idx + off;
						pchar = pattern.charAt(Math.min(pidx, M - 1));
					}
					lastIdx = idx + off;
				}

				// 		if char == pchar0 {
				// 			score := scoreMatch + bonus*bonusFirstCharMultiplier
				// 			H0sub[off] = score
				// 			C0sub[off] = 1
				// 			if M == 1 && (forward && score > maxScore || !forward && score >= maxScore) {
				// 				maxScore, maxScorePos = score, idx+off
				// 				if forward && bonus >= bonusBoundary {
				// 					break
				// 				}
				// 			}
				// 			inGap = false
				if (c == pchar0) {
					int score = SCORE_MATCH + bonus * BONUS_FIRST_CHAR_MULTIPLIER;
					H0sub[off] = score;
					C0sub[off] = 1;
					if (M == 1 && (forward && score > maxScore || !forward && score >= maxScore)) {
						maxScore = score;
						maxScorePos = idx + off;
						if (forward && bonus >= BONUS_BOUNDARY) {
							break;
						}
					}
					inGap = false;
				}
				// 		} else {
				// 			if inGap {
				// 				H0sub[off] = util.Max16(prevH0+scoreGapExtension, 0)
				// 			} else {
				// 				H0sub[off] = util.Max16(prevH0+scoreGapStart, 0)
				// 			}
				// 			C0sub[off] = 0
				// 			inGap = true
				// 		}
				// 		prevH0 = H0sub[off]
				// 	}
				else {
					if (inGap) {
						H0sub[off] = Math.max(prevH0 + SCORE_GAP_EXTENSION, 0);
					}
					else {
						H0sub[off] = Math.max(prevH0 + SCORE_GAP_START, 0);
					}
					C0sub[off] = 0;
					inGap = true;
				}
				prevH0 = H0sub[off];
		}
				// 	if pidx != M {
				// 		return Result{-1, -1, 0}, nil
				// 	}
				// 	if M == 1 {
				// 		result := Result{maxScorePos, maxScorePos + 1, int(maxScore)}
				// 		if !withPos {
				// 			return result, nil
				// 		}
				// 		pos := []int{maxScorePos}
				// 		return result, &pos
				// 	}
		if (pidx != M) {
			return SearchMatchResult.ofMinus();
		}
		if (M == 1) {
			return SearchMatchResult.of(maxScorePos, maxScorePos + 1, maxScore, new int[] { maxScorePos });
		}
				// 	// Phase 3. Fill in score matrix (H)
				// 	// Unlike the original algorithm, we do not allow omission.
				// 	f0 := int(F[0])
				// 	width := lastIdx - f0 + 1
				// 	offset16, H := alloc16(offset16, slab, width*M)
				// 	copy(H, H0[f0:lastIdx+1])
		int f0 = F[0];
		int width = lastIdx - f0 + 1;
		int[] H = new int[width * M];
		System.arraycopy(H0, f0, H, 0, lastIdx + 1 - f0);

				// 	// Possible length of consecutive chunk at each position.
				// 	_, C := alloc16(offset16, slab, width*M)
				// 	copy(C, C0[f0:lastIdx+1])
		int[] C = new int[width * M];
		System.arraycopy(C0, f0, C, 0, lastIdx + 1 - f0);

				// 	Fsub := F[1:]
				// 	Psub := pattern[1:][:len(Fsub)]
		int[] Fsub = new int[F.length - 1];
		System.arraycopy(F, 1, Fsub, 0, F.length - 1);
		// int[] Psub = new int[pattern.length() - 1 - (pattern.length() - 1 + Fsub.length)];
		String Psub = pattern.substring(1, 1 + Fsub.length);

				// 	for off, f := range Fsub {
		for (int off = 0; off < Fsub.length; off++) {
			int f = Fsub[off];

				// 		f := int(f)
				// 		pchar := Psub[off]
				// 		pidx := off + 1
				// 		row := pidx * width
				// 		inGap := false
				// 		Tsub := T[f : lastIdx+1]
				// 		Bsub := B[f:][:len(Tsub)]
				// 		Csub := C[row+f-f0:][:len(Tsub)]
				// 		Cdiag := C[row+f-f0-1-width:][:len(Tsub)]
				// 		Hsub := H[row+f-f0:][:len(Tsub)]
				// 		Hdiag := H[row+f-f0-1-width:][:len(Tsub)]
				// 		Hleft := H[row+f-f0-1:][:len(Tsub)]
				// 		Hleft[0] = 0
			char phar = Psub.charAt(off);
			pidx = off + 1;
			int row = pidx * width;
			inGap = false;
			Tsub = T.substring(f, lastIdx + 1);
			System.arraycopy(B, f, Bsub, 0, Tsub.length());
			int[] Csub = new int[C.length - (row + f - f0)];
			int[] Cdiag = new int[C.length - (row + f - f0 - 1 - width)];

				// 		for off, char := range Tsub {
				// 			col := off + f
				// 			var s1, s2, consecutive int16

				// 			if inGap {
				// 				s2 = Hleft[off] + scoreGapExtension
				// 			} else {
				// 				s2 = Hleft[off] + scoreGapStart
				// 			}

				// 			if pchar == char {
				// 				s1 = Hdiag[off] + scoreMatch
				// 				b := Bsub[off]
				// 				consecutive = Cdiag[off] + 1
				// 				if consecutive > 1 {
				// 					fb := B[col-int(consecutive)+1]
				// 					// Break consecutive chunk
				// 					if b >= bonusBoundary && b > fb {
				// 						consecutive = 1
				// 					} else {
				// 						b = util.Max16(b, util.Max16(bonusConsecutive, fb))
				// 					}
				// 				}
				// 				if s1+b < s2 {
				// 					s1 += Bsub[off]
				// 					consecutive = 0
				// 				} else {
				// 					s1 += b
				// 				}
				// 			}
				// 			Csub[off] = consecutive

				// 			inGap = s1 < s2
				// 			score := util.Max16(util.Max16(s1, s2), 0)
				// 			if pidx == M-1 && (forward && score > maxScore || !forward && score >= maxScore) {
				// 				maxScore, maxScorePos = score, col
				// 			}
				// 			Hsub[off] = score
				// 		}
				// 	}
		}

				// 	if DEBUG {
				// 		debugV2(T, pattern, F, lastIdx, H, C)
				// 	}

				// 	// Phase 4. (Optional) Backtrace to find character positions
				// 	pos := posArray(withPos, M)
				// 	j := f0
				// 	if withPos {
				// 		i := M - 1
				// 		j = maxScorePos
				// 		preferMatch := true
				// 		for {
				// 			I := i * width
				// 			j0 := j - f0
				// 			s := H[I+j0]

				// 			var s1, s2 int16
				// 			if i > 0 && j >= int(F[i]) {
				// 				s1 = H[I-width+j0-1]
				// 			}
				// 			if j > int(F[i]) {
				// 				s2 = H[I+j0-1]
				// 			}

				// 			if s > s1 && (s > s2 || s == s2 && preferMatch) {
				// 				*pos = append(*pos, j)
				// 				if i == 0 {
				// 					break
				// 				}
				// 				i--
				// 			}
				// 			preferMatch = C[I+j0] > 1 || I+width+j0+1 < len(C) && C[I+width+j0+1] > 0
				// 			j--
				// 		}
				// 	}
				// 	// Start offset we return here is only relevant when begin tiebreak is used.
				// 	// However finding the accurate offset requires backtracking, and we don't
				// 	// want to pay extra cost for the option that has lost its importance.
				// 	return Result{j, maxScorePos + 1, int(maxScore)}, pos
				// }



		return null;
	}
}
