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
package org.springframework.shell.style;

public abstract class FigureSettings {

	public final static String TAG_TICK = "tick";
	public final static String TAG_INFO = "info";
	public final static String TAG_WARNING = "warning";
	public final static String TAG_CROSS = "cross";

	public String tick() {
		return "✔"; // U+2714 Heavy Check Mark Emoji, tick, checkmark
	}

	public String info() {
		return "ℹ"; // U+2139 Information Source Emoji
	}

	public String warning() {
		return "⚠"; // U+26A0 Warning Sign Emoji, danger, аlert
	}

	public String cross() {
		return "✖"; // U+2716 Heavy Multiplication X Emoji, cross
	}

	// ☒ U+2612 Ballot Box with X
	// ☐ U+2610 Ballot Box

	// ← U+2190 Leftwards Arrow
	// ↑ U+2191 Upwards Arrow
	// → U+2192 Rightwards Arrow
	// ↓ U+2193 Downwards Arrow
	// ↔ U+2194 Left Right Arrow Emoji
	// ↕ U+2195 Up Down Arrow Emoji

	public String resolveTag(String tag) {
		switch (tag) {
			case TAG_TICK:
				return tick();
			case TAG_INFO:
				return info();
			case TAG_WARNING:
				return warning();
			case TAG_CROSS:
				return cross();
		}
		throw new IllegalArgumentException(String.format("Unknown tag '%s'", tag));
	}

	public static FigureSettings figureSettings() {
		return new DefaultFigureSettings();
	}

	public static FigureSettings dump() {
		return new DumpFigureSettings();
	}

	public static String[] tags() {
		return new String[] {
				TAG_TICK,
				TAG_INFO,
				TAG_WARNING,
				TAG_CROSS
		};
	}

	private static class DefaultFigureSettings extends FigureSettings {
	}

	private static class DumpFigureSettings extends FigureSettings {

		@Override
		public String tick() {
			return "v";
		}

		@Override
		public String info() {
			return "i";
		}

		@Override
		public String warning() {
			return "!";
		}

		@Override
		public String cross() {
			return "x";
		}
	}
}
