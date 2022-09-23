package org.springframework.shell.support.search;

public interface SearchMatchResult {

	int getStart();
	int getEnd();
	int getScore();
	int[] getPositions();

	public static SearchMatchResult of(int start, int end, int score, int[] positions) {
		return new DefaultResult(start, end, score, positions);
	}

	static class DefaultResult implements SearchMatchResult {

		int start;
		int end;
		int score;
		int[] positions;

		DefaultResult(int start, int end, int score, int[] positions) {
			this.start = start;
			this.end = end;
			this.score = score;
			this.positions = positions;
		}

		@Override
		public int getStart() {
			return start;
		}

		@Override
		public int getEnd() {
			return end;
		}

		@Override
		public int getScore() {
			return score;
		}

		@Override
		public int[] getPositions() {
			return positions;
		}
	}

}
