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
import org.jline.reader.LineReader;

import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.shell.Shell;
import org.springframework.shell.context.DefaultShellContext;
import org.springframework.shell.jline.InteractiveShellRunner;
import org.springframework.shell.jline.PromptProvider;

/**
 * Client for terminal session which can be used as a programmatic way
 * to interact with a shell application. In a typical test it is required
 * to write into a shell and read what is visible in a shell.
 *
 * @author Janne Valkealahti
 */
public interface ShellClient {

	void write(String data);

	String read();

	void start();

	void stop();

	public static Builder builder() {
		return new DefaultBuilder();
	}

	interface Builder {

		Builder xxx(TerminalSession terminalSession, Shell shell, PromptProvider promptProvider, LineReader lineReader);

		ShellClient build();
	}

	static class DefaultBuilder implements Builder {

		private TerminalSession terminalSession;
		private Shell shell;
		private PromptProvider promptProvider;
		private LineReader lineReader;

		@Override
		public Builder xxx(TerminalSession terminalSession, Shell shell, PromptProvider promptProvider, LineReader lineReader) {
			this.terminalSession = terminalSession;
			this.shell = shell;
			this.promptProvider = promptProvider;
			this.lineReader = lineReader;
			return this;
		}

		@Override
		public ShellClient build() {
			DefaultShellClient client = new DefaultShellClient(this.terminalSession, this.shell, this.promptProvider,
					this.lineReader);
			return client;
		}

	}

	static class DefaultShellClient implements ShellClient {

		private TerminalSession terminalSession;
		private Shell shell;
		private PromptProvider promptProvider;
		private LineReader lineReader;
		private Thread runnerThread;


		DefaultShellClient(TerminalSession terminalSession, Shell shell, PromptProvider promptProvider, LineReader lineReader) {
			this.terminalSession = terminalSession;
			this.shell = shell;
			this.promptProvider = promptProvider;
			this.lineReader = lineReader;
		}

		@Override
		public void write(String data) {
			terminalSession.getTerminalStarter().sendString(data);
		}

		@Override
		public String read() {
			String screen = terminalSession.getTerminalTextBuffer().getScreenLines();
			return screen;
		}

		@Override
		public void start() {
			this.terminalSession.start();
			if (this.runnerThread == null) {
				this.runnerThread = new Thread(new ShellRunnerTask(this.shell, this.promptProvider, this.lineReader));
				this.runnerThread.start();
			}
		}

		@Override
		public void stop() {
			if (this.runnerThread != null) {
				this.runnerThread.interrupt();
			}
			this.terminalSession.close();
		}
	}

	static class ShellRunnerTask implements Runnable {

		Shell shell;
		PromptProvider promptProvider;
		LineReader lineReader;

		public ShellRunnerTask(Shell shell, PromptProvider promptProvider, LineReader lineReader) {
			this.shell = shell;
			this.promptProvider = promptProvider;
			this.lineReader = lineReader;
		}

		@Override
		public void run() {
			InteractiveShellRunner runner = new InteractiveShellRunner(lineReader, promptProvider, shell,
					new DefaultShellContext());
			try {
				runner.run(new DefaultApplicationArguments());
			} catch (Throwable e) {
			}
		}

	}


}
