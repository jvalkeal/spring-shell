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
package org.springframework.shell.test.autoconfigure;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;

import com.jediterm.support.TestTerminalSession;
import org.jline.terminal.Terminal;
import org.jline.terminal.impl.DumbTerminal;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.shell.test.ShellClient;

@AutoConfiguration
public class ShellAutoConfiguration {

	/*
	 * Configure terminal with input/output so that we have a control
	 * in tests to track what happens with interaction.
	 */
	@Bean
	Terminal terminal(TestTerminalSession session) throws IOException {
		PipedInputStream pipedInputStream = new PipedInputStream();
		PipedOutputStream pipedOutputStream = new PipedOutputStream();


		// TestTerminalSession session = new TestTerminalSession(30, 3);
		DumbTerminal terminal = new DumbTerminal("terminal", "ansi", pipedInputStream, pipedOutputStream,
				StandardCharsets.UTF_8);
		return terminal;
	}

	@Bean
	TestTerminalSession testTerminalSession() {
		TestTerminalSession session = new TestTerminalSession(30, 3);
		return session;
	}

	@Bean
	ShellClient shellClient(TestTerminalSession testTerminalSession) {
		return ShellClient.builder().build();
	}

}
