package org.springframework.shell.support.search;

public interface SearchMatch {

	SearchMatchResult match(String text, String pattern);

	public static Builder builder() {
		return new DefaultBuilder();
	}

	public interface Builder {
		Builder caseSensitive(boolean caseSensitive);
		Builder normalize(boolean normalize);
		Builder forward(boolean forward);
		SearchMatch build();
	}

	static class DefaultBuilder implements Builder {

		private boolean caseSensitive;
		private boolean normalize;
		private boolean forward;

		@Override
		public Builder caseSensitive(boolean caseSensitive) {
			this.caseSensitive = caseSensitive;
			return this;
		}

		@Override
		public Builder normalize(boolean normalize) {
			this.normalize = normalize;
			return this;
		}

		@Override
		public Builder forward(boolean forward) {
			this.forward = forward;
			return this;
		}

		@Override
		public SearchMatch build() {
			return new DefaultSearchMatch(this.caseSensitive, this.normalize, this.forward);
		}
	}

	static class DefaultSearchMatch implements SearchMatch {

		private boolean caseSensitive;
		private boolean normalize;
		private boolean forward;
		private boolean withPos;

		DefaultSearchMatch(boolean caseSensitive, boolean normalize, boolean forward) {
			this.caseSensitive = caseSensitive;
			this.normalize = normalize;
			this.forward = forward;
		}

		@Override
		public SearchMatchResult match(String text, String pattern) {
			FuzzyMatchV1SearchMatchAlgorithm algo = new FuzzyMatchV1SearchMatchAlgorithm();
			return algo.match(caseSensitive, normalize, forward, text, pattern, withPos);
		}
	}
}
