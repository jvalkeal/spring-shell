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

import java.io.IOException;
import java.util.List;

import com.jediterm.support.TestTerminalSession;

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

	interface Builder {

		Builder session(TestTerminalSession testTerminalSession);

		ShellClient build();
	}

	static class DefaultBuilder implements Builder {

		private TestTerminalSession testTerminalSession;

		@Override
		public Builder session(TestTerminalSession testTerminalSession) {
			this.testTerminalSession = testTerminalSession;
			return this;
		}

		@Override
		public ShellClient build() {
			DefaultShellClient client = new DefaultShellClient();
			return client;
		}

	}

	static class DefaultShellClient implements ShellClient {

		private TestTerminalSession testTerminalSession;

		@Override
		public void write(String data) {
			try {
				this.testTerminalSession.process(data);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public String read() {
			return this.testTerminalSession.getTerminal().getTextBuffer().getScreenLines();
		}
	}

}
