package org.springframework.shell.style;

import java.util.Arrays;
import java.util.List;

public class PartsText {

	private List<PartText> parts;

	PartsText(List<PartText> parts) {
		this.parts = parts;
	}

	public static PartsText of(PartText... parts) {
		return new PartsText(Arrays.asList(parts));
	}

	public static PartsText of(List<PartText> parts) {
		return new PartsText(parts);
	}

	public List<PartText> getParts() {
		return parts;
	}

	public static class PartText {

		private String text;
		private boolean match;

		public PartText(String text, boolean match) {
			this.text = text;
			this.match = match;
		}

		public static PartText of(String text, boolean match) {
			return new PartText(text, match);
		}

		public String getText() {
			return text;
		}

		public boolean isMatch() {
			return match;
		}
	}
}
