/*
 * Copyright 2023 the original author or authors.
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
package org.springframework.shell.component.view.event;

import java.util.ArrayList;
import java.util.EnumSet;

public record KeyEvent(String data, KeyType key, EnumSet<ModType> mod) {

	public static KeyEvent ofCharacter(String data) {
		return new KeyEvent(data, null, EnumSet.noneOf(ModType.class));
	}

	public static KeyEvent ofCharacter(String data, EnumSet<ModType> mod) {
		return new KeyEvent(data, null, mod);
	}

	public static KeyEvent ofType(KeyType type) {
		return new KeyEvent(null, type, EnumSet.noneOf(ModType.class));
	}

	public static KeyEvent ofType(KeyType type, EnumSet<ModType> mod) {
		return new KeyEvent(null, type, mod);
	}

	public enum ModType {
		CTRL,
		ALT,
		SHIFT;

		public static EnumSet<ModType> of(boolean ctrl, boolean alt, boolean shift) {
			if (!ctrl && !alt && !shift) {
				return EnumSet.noneOf(ModType.class);
			}
			ArrayList<ModType> list = new ArrayList<ModType>();
			if (ctrl) {
				list.add(CTRL);
			}
			if (alt) {
				list.add(ALT);
			}
			if (shift) {
				list.add(SHIFT);
			}
			return EnumSet.copyOf(list);
		}
	}

	public enum KeyType {
		CHAR("Character"),
		BACKTAB("BACKTAB"),
		ENTER("Enter"),
		TAB("Tab"),
		DOWN("DownArrow"),
		UP("UpArrow"),
		LEFT("LeftArrow"),
		RIGHT("RightArrow"),
		BACKSPACE("Backspace"),
		DELETE("Delete");

		private final String name;

		KeyType(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	//     mask         special keys                  unicode keys                    ascii keys
	// [         ] [                     ] [                                 ] [                     ]
	// 32 31 30 29 28 27 26 25 24 23 22 21 20 19 18 17 16 15 14 13 12 11 10 09 08 07 06 05 04 03 02 01

	public static class Key {

		public static final int A = 65;
		public static final int Ax = 0x41;
		public static final int a = 97;

		public static final int t2 = 0x00000000;
		public static final int t3 = 0xffffffff;

		public static final int CharMask = 0x000fffff;
		public static final int SpecialMask = 0xfff00000;

		public static final int ShiftMask = 0x10000000;
		public static final int CtrlMask = 0x40000000;
		public static final int AltMask = 0x80000000;


		public static final int CursorUp = 0x100000;
		public static final int CursorDown = 0x100001;
		public static final int CursorLeft = 0x100002;
		public static final int CursorRight = 0x100003;
	}
}
