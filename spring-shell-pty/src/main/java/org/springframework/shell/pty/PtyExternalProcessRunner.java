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
package org.springframework.shell.pty;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.shell.command.ExternalProcessRunner;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

/**
 * {@link ExternalProcessRunner} implementation using {@code pty4j} from {@code JetBrains}.
 *
 * @author Janne Valkealahti
 */
public class PtyExternalProcessRunner implements ExternalProcessRunner {

	private final static Logger log = LoggerFactory.getLogger(PtyExternalProcessRunner.class);

	@Override
	public void run(String[] cmd) {
		log.debug("Running command {}", StringUtils.arrayToCommaDelimitedString(cmd));
		Map<String, String> env = new HashMap<>(System.getenv());
		// env.put("TERM", "xterm-256color");
		try {
			PtyProcess process = new PtyProcessBuilder()
				.setCommand(cmd)
				.setEnvironment(env)
				.setInitialColumns(120)
				// .setWindowsAnsiColorEnabled(true)
				.setConsole(true)
				.start();
			OutputStream os = process.getOutputStream();
			InputStream is = process.getInputStream();

			Thread thread = new Thread(() -> {
				try {
					String data = StreamUtils.copyToString(is, Charset.defaultCharset());
					System.out.println(data);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});

			thread.run();

			int result = process.waitFor();
			log.debug("Result from external command {}", result);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
