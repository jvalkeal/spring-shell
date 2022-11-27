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
package org.springframework.shell.test.jediterm.terminal.model;

import java.util.concurrent.TimeUnit;

import org.springframework.shell.test.jediterm.terminal.TextStyle;

/**
 * @author jediterm authors
 */
public final class TerminalTypeAheadSettings {

	public static final TerminalTypeAheadSettings DEFAULT = new TerminalTypeAheadSettings(
		true,
		TimeUnit.MILLISECONDS.toNanos(100),
		new TextStyle(null)
	);

	private final boolean myEnabled;
	private final long myLatencyThreshold;
	private final TextStyle myTypeAheadStyle;

	public TerminalTypeAheadSettings(boolean enabled, long latencyThreshold, TextStyle typeAheadColor) {
		myEnabled = enabled;
		myLatencyThreshold = latencyThreshold;
		myTypeAheadStyle = typeAheadColor;
	}

	public boolean isEnabled() {
		return myEnabled;
	}

	public long getLatencyThreshold() {
		return myLatencyThreshold;
	}

	public TextStyle getTypeAheadStyle() {
		return myTypeAheadStyle;
	}
}
