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

import java.io.Closeable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.shell.Shell;
import org.springframework.shell.ShellRunner;
import org.springframework.shell.context.DefaultShellContext;
import org.springframework.shell.jline.InteractiveShellRunner;
import org.springframework.shell.jline.NonInteractiveShellRunner;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.shell.test.jediterm.terminal.ui.TerminalSession;

/**
 * Client for terminal session which can be used as a programmatic way
 * to interact with a shell application. In a typical test it is required
 * to write into a shell and read what is visible in a shell.
 *
 * @author Janne Valkealahti
 */
public interface ShellClient extends Closeable {

	/**
	 * Run interactive shell session.
	 *
	 * @return client for chaining
	 */
	ShellClient runInterative();

	/**
	 * Run non-interactive command session.
	 *
	 * @param args the command arguments
	 * @return client for chaining
	 */
	ShellClient runNonInterative(String... args);

	/**
	 * Read the screen.
	 *
	 * @return the screen
	 */
	ShellScreen screen();

	/**
	 * Write plain text into a shell.
	 *
	 * @param text the text
	 * @return client for chaining
	 */
	ShellClient write(String text);

	/**
	 * Close and release resources for previously run shell or command session.
	 */
	@Override
	void close();

	/**
	 * Get a write sequencer.
	 *
	 * @return a write sequencer
	 */
	ShellWriteSequence writeSequence();

	/**
	 * Get an instance of a builder.
	 *
	 * @param terminalSession the terminal session
	 * @param shell the shell
	 * @param promptProvider the prompt provider
	 * @param lineReader the line reader
	 * @param terminal the terminal
	 * @return a Builder
	 */
	public static Builder builder(TerminalSession terminalSession, Shell shell, PromptProvider promptProvider,
			LineReader lineReader, Terminal terminal) {
		return new DefaultBuilder(terminalSession, shell, promptProvider, lineReader, terminal);
	}

	/**
	 * Builder interface for {@code ShellClient}.
	 */
	interface Builder {

		/**
		 * Build a shell client.
		 *
		 * @return a shell client
		 */
		ShellClient build();
	}

	static class DefaultBuilder implements Builder {

		private TerminalSession terminalSession;
		private Shell shell;
		private PromptProvider promptProvider;
		private LineReader lineReader;
		private Terminal terminal;

		DefaultBuilder(TerminalSession terminalSession, Shell shell, PromptProvider promptProvider,
				LineReader lineReader, Terminal terminal) {
			this.terminalSession = terminalSession;
			this.shell = shell;
			this.promptProvider = promptProvider;
			this.lineReader = lineReader;
			this.terminal = terminal;
		}

		@Override
		public ShellClient build() {
			DefaultShellClient client = new DefaultShellClient(this.terminalSession, this.shell, this.promptProvider,
					this.lineReader, this.terminal);
			return client;
		}
	}

	static class DefaultShellClient implements ShellClient {

		private final static Logger log = LoggerFactory.getLogger(DefaultShellClient.class);

		private TerminalSession terminalSession;
		private Shell shell;
		private PromptProvider promptProvider;
		private LineReader lineReader;
		private Thread runnerThread;
		private Terminal terminal;
		// private boolean interactive = false;

		DefaultShellClient(TerminalSession terminalSession, Shell shell, PromptProvider promptProvider,
				LineReader lineReader, Terminal terminal) {
			this.terminalSession = terminalSession;
			this.shell = shell;
			this.promptProvider = promptProvider;
			this.lineReader = lineReader;
			this.terminal = terminal;
		}

		@Override
		public ShellClient runInterative() {
			terminalSession.start();
			if (runnerThread == null) {
				// runnerThread = new Thread(new InteractiveShellRunnerTask(this.shell, this.promptProvider, this.lineReader));
				runnerThread = new Thread(new ShellRunnerTask(this.blockingQueue));
				runnerThread.start();
			}
			ShellRunner runner = new InteractiveShellRunner(lineReader, promptProvider, shell, new DefaultShellContext());
			ApplicationArguments argsx = new DefaultApplicationArguments();
			this.blockingQueue.add(new ShellRunnerTaskData(runner, argsx));
			return this;
		}

		@Override
		public ShellClient runNonInterative(String... args) {
			terminalSession.start();
			if (runnerThread == null) {
				// runnerThread = new Thread(new NonInteractiveShellRunnerTask(this.shell, args));
				runnerThread = new Thread(new ShellRunnerTask(this.blockingQueue));
				runnerThread.start();
			}
			ShellRunner runner = new NonInteractiveShellRunner(shell, new DefaultShellContext());
			ApplicationArguments argsx = new DefaultApplicationArguments(args);
			this.blockingQueue.add(new ShellRunnerTaskData(runner, argsx));
			return this;
		}

		@Override
		public ShellClient write(String data) {
			terminalSession.getTerminalStarter().sendString(data);
			return this;
		}

		@Override
		public ShellScreen screen() {
			return ShellScreen.of(terminalSession.getTerminalTextBuffer().getScreen());
		}

		@Override
		public void close() {
			write(writeSequence().ctrl('c').build());
			// this.blockingQueue.add(new ShellRunnerTaskData(null, null));
			// if (runnerThread != null) {
			// 	runnerThread.interrupt();
			// 	try {
			// 		runnerThread.join();
			// 	} catch (InterruptedException e) {
			// 		e.printStackTrace();
			// 	}
			// }
			// terminalSession.close();
			// runnerThread = null;
		}

		@Override
		public ShellWriteSequence writeSequence() {
			return ShellWriteSequence.of(DefaultShellClient.this.terminal);
		}

		BlockingQueue<ShellRunnerTaskData> blockingQueue = new LinkedBlockingDeque<>(10);
	}



	static record ShellRunnerTaskData(
		ShellRunner runner,
		ApplicationArguments args
	) {}

	static class ShellRunnerTask implements Runnable {

		private final static Logger log = LoggerFactory.getLogger(ShellRunnerTask.class);

		BlockingQueue<ShellRunnerTaskData> blockingQueue;

		ShellRunnerTask(BlockingQueue<ShellRunnerTaskData> blockingQueue) {
			this.blockingQueue = blockingQueue;
		}

		@Override
		public void run() {
			log.info("ShellRunnerTask start");
			try {
				Thread.currentThread().setName("ShellRunnerTask");
				while (true) {
					ShellRunnerTaskData data = blockingQueue.take();
					if (data.runner == null) {
						return;
					}
					try {
						log.info("Running {}", data.runner());
						data.runner().run(data.args());
						log.info("Running done {}", data.runner());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			log.info("ShellRunnerTask end");
		}

	}


	// static class InteractiveShellRunnerTask implements Runnable {

	// 	Shell shell;
	// 	PromptProvider promptProvider;
	// 	LineReader lineReader;

	// 	public InteractiveShellRunnerTask(Shell shell, PromptProvider promptProvider, LineReader lineReader) {
	// 		this.shell = shell;
	// 		this.promptProvider = promptProvider;
	// 		this.lineReader = lineReader;
	// 	}

	// 	@Override
	// 	public void run() {
	// 		InteractiveShellRunner runner = new InteractiveShellRunner(lineReader, promptProvider, shell,
	// 				new DefaultShellContext());
	// 		try {
	// 			runner.run(new DefaultApplicationArguments());
	// 		} catch (Throwable e) {
	// 		}
	// 	}

	// }

	// static class NonInteractiveShellRunnerTask implements Runnable {

	// 	private Shell shell;
	// 	private String[] args;

	// 	public NonInteractiveShellRunnerTask(Shell shell, String[] args) {
	// 		this.shell = shell;
	// 		this.args = args;
	// 	}

	// 	@Override
	// 	public void run() {
	// 		NonInteractiveShellRunner runner = new NonInteractiveShellRunner(shell, new DefaultShellContext());
	// 		try {
	// 			runner.run(new DefaultApplicationArguments(this.args));
	// 		} catch (Throwable e) {
	// 		}
	// 	}
	// }
}
