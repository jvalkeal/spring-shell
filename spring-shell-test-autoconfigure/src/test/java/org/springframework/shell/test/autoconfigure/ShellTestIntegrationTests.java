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

import java.util.concurrent.TimeUnit;

import com.jediterm.terminal.ui.JediTermWidget;
import org.jline.reader.LineReader;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.shell.Shell;
import org.springframework.shell.boot.ShellContextAutoConfiguration;
import org.springframework.shell.context.DefaultShellContext;
import org.springframework.shell.context.ShellContext;
import org.springframework.shell.jline.InteractiveShellRunner;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.shell.test.autoconfigure.ShellAutoConfiguration.TerminalStreams;
import org.springframework.shell.test.autoconfigure.app.ExampleShellApplication;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@ShellTest
@ContextConfiguration(classes = ExampleShellApplication.class)
public class ShellTestIntegrationTests {

	@Autowired
	JediTermWidget widget;

	@Autowired
	TerminalStreams terminalStreams;

	@Autowired
	Shell shell;

	@Autowired
	PromptProvider promptProvider;

	@Autowired
	LineReader lineReader;


	@Test
	void test() throws Exception {
		assertThat(widget).isNotNull();

		runTask();

		widget.getTerminalStarter().sendString("help\n");


		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			String screen = widget.getTerminalTextBuffer().getScreenLines();
			assertThat(screen).contains("AVAILABLE COMMANDS");
		});

		// String screen1 = widget.getTerminalTextBuffer().getScreenLines();
		// String screen2 = widget.getTerminalTextBuffer().getScreenLines();
		// String screen3 = widget.getTerminalTextBuffer().getScreenLines();

		// widget.getTerminalStarter().sendString("help\n");

		// String screen4 = widget.getTerminalTextBuffer().getScreenLines();
		// String screen5 = widget.getTerminalTextBuffer().getScreenLines();

	}

	private void runTask() {
		Thread myThread = new Thread(new TestTask());
		myThread.start();
	}

	class TestTask implements Runnable {

		@Override
		public void run() {
			InteractiveShellRunner runner = new InteractiveShellRunner(lineReader, promptProvider, shell,
					new DefaultShellContext());
			try {
				runner.run(new DefaultApplicationArguments());
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}
