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

import org.springframework.util.Assert;

import static org.jline.keymap.KeyMap.alt;
import static org.jline.keymap.KeyMap.ctrl;
import static org.jline.keymap.KeyMap.key;

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
		bindExpression("DownArrow", keyMap);
		bindExpression("UpArrow", keyMap);
		bindExpression("LeftArrow", keyMap);
		bindExpression("RightArrow", keyMap);
		bindExpression("PageUp", keyMap);
		bindExpression("PageDown", keyMap);
	}

	public void bindExpression(String expression, KeyMap<String> keyMap) {
		String exp = expression.toLowerCase(Locale.ROOT);
		String function = prefix + expression;
		String keySeq = null;
		ExpressionResult result = parseExpression(exp);
		switch (exp) {
			case "downarrow":
				keySeq = key(terminal, Capability.key_down);
				break;
			case "uparrow":
				keySeq = key(terminal, Capability.key_up);
				break;
			case "leftarrow":
				keySeq = key(terminal, Capability.key_left);
				break;
			case "rightarrow":
				keySeq = key(terminal, Capability.key_right);
				break;
			case "pageup":
				keySeq = key(terminal, Capability.key_ppage);
				break;
			case "pagedown":
				keySeq = key(terminal, Capability.key_npage);
				break;
			default:
				break;
		}
		if (result.ctrl() && result.key() != null && result.key().length() == 1) {
			keySeq = ctrl(result.key().charAt(0));
		}
		if (result.alt()) {
			keySeq = alt(keySeq);
		}
		if (function != null && keySeq != null) {
			keyMap.bind(function, keySeq);
		}
	}

	public record ExpressionResult(String key, boolean ctrl, boolean alt) {}

	public ExpressionResult parseExpression(String expression) {
		boolean ctrl = false;
		boolean alt = false;
		String key = null;
		for (String part : expression.split("\\+")) {
			part = part.strip();
			if ("ctrl".equals(part)) {
				ctrl = true;
			}
			else if ("alt".equals(part)) {
				alt = true;
			}
			else {
				key = part;
			}
		}
		return new ExpressionResult(key, ctrl, alt);
	}

}
