/*
 * Copyright 2024 the original author or authors.
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
package org.springframework.shell.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.util.StringUtils;

/**
 * Interface to run external command outside of a {@code Spring Shell}.
 *
 * @author Janne Valkealahti
 */
public interface ExternalProcessRunner {

	/**
	 * Execute command with given array.
	 *
	 * @param cmd the command array
	 */
	void run(String[] cmd);

	/**
	 * Default implementation of a {@link ExternalProcessRunner} simply using plain
	 * jdk's features. This implementation is "dummy" meaning it doesn't provide
	 * any {@code pty} features meaning possible target command cannot use any
	 * higher level shell features, thus being "dummy". For example in linux if you
	 * output "ls" command into file using process output redirect it detects non pty
	 * environment and goes into "dummy" mode.
	 */
	static class DefaultExternalProcessRunner implements ExternalProcessRunner {

		private final static Logger log = LoggerFactory.getLogger(DefaultExternalProcessRunner.class);

		@Override
		public void run(String[] cmd) {
			log.debug("Running command {}", StringUtils.arrayToCommaDelimitedString(cmd));
			ProcessBuilder builder = new ProcessBuilder();
			builder.command(cmd);
			try {
				Process process = builder.start();
				StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), System.out::println);

				Thread thread = new Thread(() -> {
					try {
						streamGobbler.run();
					} catch (Exception e) {
						e.printStackTrace();
					}
				});

				thread.run();
				int exitCode = process.waitFor();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}

		private static class StreamGobbler implements Runnable {
			private InputStream inputStream;
			private Consumer<String> consumer;

			public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
				this.inputStream = inputStream;
				this.consumer = consumer;
			}

			@Override
			public void run() {
				new BufferedReader(new InputStreamReader(inputStream)).lines()
				  .forEach(consumer);
			}
		}

	}


}
