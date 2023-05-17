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
package org.springframework.shell.component.view;

import java.util.Locale;

import org.jline.keymap.KeyMap;
import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp.Capability;

import org.springframework.shell.component.view.KeyEvent.KeyType;
import org.springframework.shell.component.view.KeyEvent.ModType;
import org.springframework.util.Assert;

import static org.jline.keymap.KeyMap.alt;
import static org.jline.keymap.KeyMap.del;
import static org.jline.keymap.KeyMap.ctrl;
import static org.jline.keymap.KeyMap.key;
import static org.jline.keymap.KeyMap.translate;

public class KeyBinder {

	public final static String DEFAULT_PREFIX = "OPERATION_EXP_";
	private final Terminal terminal;
	private final String prefix;

	public KeyBinder(Terminal terminal) {
		this(terminal, null);
	}

	public KeyBinder(Terminal terminal, String prefix) {
		Assert.notNull(terminal, "terminal must be set");
		this.terminal = terminal;
		this.prefix = prefix == null ? DEFAULT_PREFIX : prefix;
	}

	public String getPrefix() {
		return prefix;
	}

	public void bindAll(KeyMap<String> keyMap) {

		keyMap.bind("OPERATION_KEY_ENTER", "\r");
		keyMap.bind("OPERATION_KEY_BACKSPACE", del());
		keyMap.bind("OPERATION_KEY_DELETE", key(terminal, Capability.key_dc));
		keyMap.bind("OPERATION_KEY_TAB", "\t");
		keyMap.bind("OPERATION_KEY_BACKTAB", key(terminal, Capability.key_btab));

		keyMap.bind("OPERATION_KEY_LEFT", key(terminal, Capability.key_left));
		keyMap.bind("OPERATION_KEY_RIGHT", key(terminal, Capability.key_right));
		keyMap.bind("OPERATION_KEY_UP", key(terminal, Capability.key_up));
		keyMap.bind("OPERATION_KEY_DOWN", key(terminal, Capability.key_down));

		keyMap.bind("OPERATION_KEY_ALT_LEFT", alt(key(terminal, Capability.key_left)));
		keyMap.bind("OPERATION_KEY_ALT_RIGHT", alt(key(terminal, Capability.key_right)));
		keyMap.bind("OPERATION_KEY_ALT_UP", alt(key(terminal, Capability.key_up)));
		keyMap.bind("OPERATION_KEY_ALT_DOWN", alt(key(terminal, Capability.key_down)));

		keyMap.bind("OPERATION_KEY_CTRL_LEFT", translate("^[[1;5D"));
		keyMap.bind("OPERATION_KEY_CTRL_RIGHT", translate("^[[1;5C"));
		keyMap.bind("OPERATION_KEY_CTRL_UP", translate("^[[1;5A"));
		keyMap.bind("OPERATION_KEY_CTRL_DOWN", translate("^[[1;5B"));

		keyMap.bind("OPERATION_KEY_SHIFT_LEFT", translate("^[[1;2D"));
		keyMap.bind("OPERATION_KEY_SHIFT_RIGHT", translate("^[[1;2C"));
		keyMap.bind("OPERATION_KEY_SHIFT_UP", translate("^[[1;2A"));
		keyMap.bind("OPERATION_KEY_SHIFT_DOWN", translate("^[[1;2B"));

	}

	public KeyEvent parseKeyEvent(String expression) {
		boolean ctrl = false;
		boolean alt = false;
		boolean shift = false;
		String key = null;
		for (String part : expression.split("_")) {
			part = part.strip();
			if ("CTRL".equals(part)) {
				ctrl = true;
			}
			else if ("ALT".equals(part)) {
				alt = true;
			}
			else if ("SHIFT".equals(part)) {
				shift = true;
			}
			else {
				key = part;
			}
		}
		KeyType keyType = KeyType.valueOf(key);
		KeyEvent keyEvent = KeyEvent.ofType(keyType, ModType.of(ctrl, alt, shift));
		return keyEvent;
	}

}
