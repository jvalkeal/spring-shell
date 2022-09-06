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
package org.springframework.shell.test;

import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.TerminalSession;

import org.springframework.shell.ShellRunner;

/**
 * Client for terminal session which can be used as a programmatic way
 * to interact with a shell application. In a typical test it is required
 * to write into a shell are read what is visible in a shell.
 *
 * @author Janne Valkealahti
 */
public interface ShellClient {

	void write(String data);

	String read();

	public static Builder builder() {
		return new DefaultBuilder();
	}

	enum Mode {
		INTERACTIVE,
		NONINTERACTIVE
	}

	interface Builder {

		Builder mode(Mode mode);

		ShellClient build();
	}

	static class DefaultBuilder implements Builder {

		private Mode mode;

		@Override
		public Builder mode(Mode mode) {
			this.mode = mode;
			return this;
		}

		@Override
		public ShellClient build() {
			DefaultShellClient client = new DefaultShellClient();
			return client;
		}

	}

	static class DefaultShellClient implements ShellClient {

		private TerminalSession widget;
		private ShellRunner shellRunner;

		DefaultShellClient() {

		}

		@Override
		public void write(String data) {
			widget.getTerminalStarter().sendString(data);
		}

		@Override
		public String read() {
			String screen = widget.getTerminalTextBuffer().getScreenLines();
			return screen;
		}

		public void start() {

		}

		public void stop() {

		}
	}

}
