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
package org.springframework.shell.samples;

import java.util.concurrent.TimeUnit;

import org.jline.keymap.KeyMap;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.shell.samples.standard.ResolvedCommands;
import org.springframework.shell.samples.standard.TableCommands;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.test.ShellClient;
import org.springframework.shell.test.autoconfigure.ShellTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

// @ShellTest(includeFilters = { @Filter(ShellComponent.class) })
@ShellTest
@Import(ResolvedCommands.ResolvedCommandsConfiguration.class)
public class SpringShellSampleTests {

	private final static Logger log = LoggerFactory.getLogger(SpringShellSampleTests.class);

	@Autowired
	ShellClient.Builder builder;

	@Test
	void test() {
		ShellClient client = builder.build();
		client.shell();
		client.write("component single\n");

		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			String screen = client.screen();
			assertThat(screen).contains("❯ key1");
		});

		// client.write("\u001b[1B");
		client.write("\\x1B[1B");
		// client.write(KeyMap.ctrl('E'));

		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			String screen = client.screen();
			assertThat(screen).contains("❯ key2");
		});

	}
}
