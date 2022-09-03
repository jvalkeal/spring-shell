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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;

import com.jediterm.terminal.Questioner;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.terminal.impl.DumbTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.shell.boot.JLineShellAutoConfiguration;

@AutoConfiguration(before = JLineShellAutoConfiguration.class)
public class ShellAutoConfiguration {

	/*
	 * Configure terminal with input/output so that we have a control
	 * in tests to track what happens with interaction.
	 */
	@Bean
	Terminal terminal(TerminalStreams terminalStreams) throws IOException {
		Terminal terminal = TerminalBuilder.builder()
			.streams(terminalStreams.input, terminalStreams.output)
			.build();
		// DumbTerminal terminal = new DumbTerminal("terminal", "ansi", terminalStreams.input,
		// 		terminalStreams.output, StandardCharsets.UTF_8);


		return terminal;
	}

	@Bean
	TerminalStreams terminalStreams() {
		return new TerminalStreams();
	}

	@Bean
	TtyConnector ttyConnector(TerminalStreams terminalStreams) {
		return new TestTtyConnector(terminalStreams.input, terminalStreams.output);
	}

	@Bean
	JediTermWidget jediTermWidget(TtyConnector ttyConnector) {
		JediTermWidget widget = new JediTermWidget(80, 24, new DefaultSettingsProvider());
		widget.setTtyConnector(ttyConnector);
		widget.start();
		return widget;
	}

	public static class TerminalStreams {
		PipedInputStream input = new PipedInputStream();
		PipedOutputStream output = new PipedOutputStream();
	}

	private static class TestTtyConnector implements TtyConnector {

		private final static Logger log = LoggerFactory.getLogger(TestTtyConnector.class);

		PipedInputStream input;
		PipedOutputStream output;
		InputStreamReader myReader;
		// PipedOutputStream xxx;
		PipedInputStream xxx;
		PipedOutputStream ddd;
		OutputStreamWriter myWriter;

		TestTtyConnector(PipedInputStream input, PipedOutputStream output) {
			this.input = input;
			this.output = output;
			try {
				this.xxx = new PipedInputStream(output);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				this.ddd = new PipedOutputStream(input);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			this.myReader = new InputStreamReader(this.xxx);
			this.myWriter = new OutputStreamWriter(this.ddd);
			// this.xxx = new PipedOutputStream(arg0)
		}

		@Override
		public boolean init(Questioner q) {
			return true;
		}

		@Override
		public void close() {
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public int read(char[] buf, int offset, int length) throws IOException {
			log.info("XXX read1");

			int read = this.myReader.read(buf, offset, length);
			log.info("XXX read2 {}", read);
			return read;
			// return this.myReader.read(buf, offset, length);
		}

		@Override
		public void write(byte[] bytes) throws IOException {
			log.info("XXX write1 {}", bytes);
			// this.output.write(bytes);
			// this.output.flush();
			// this.ddd.write(bytes);
			this.myWriter.write(new String(bytes));
			log.info("XXX write2");
		}

		@Override
		public boolean isConnected() {
			return true;
		}

		@Override
		public void write(String string) throws IOException {
			this.write(string.getBytes());
		}

		@Override
		public int waitFor() throws InterruptedException {
			return 0;
		}

		@Override
		public boolean ready() throws IOException {
			log.info("XXX ready1");
			boolean ready = myReader.ready();
			log.info("XXX ready2 {}", ready);
			return ready;
			// return myReader.ready();
			// return true;
		}
	}

}
