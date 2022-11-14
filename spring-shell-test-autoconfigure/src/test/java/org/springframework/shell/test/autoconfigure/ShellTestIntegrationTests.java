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

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.test.ShellAssertions;
import org.springframework.shell.test.ShellClient;
import org.springframework.shell.test.ShellScreen;
import org.springframework.shell.test.ShellClient.InteractiveShellSession;
import org.springframework.shell.test.ShellClient.NonInteractiveShellSession;
import org.springframework.shell.test.autoconfigure.app.ExampleShellApplication;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@ShellTest
@ContextConfiguration(classes = ExampleShellApplication.class)
public class ShellTestIntegrationTests {

	@Autowired
	ShellClient client;

	@Test
	void testInteractive1() throws Exception {
		InteractiveShellSession session = client.interactive().run();

		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			ShellAssertions.assertThat(session.screen()).containsText("shell");
		});

		session.write("help\n");
		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			ShellAssertions.assertThat(session.screen()).containsText("AVAILABLE COMMANDS");
		});

		session.write(session.writeSequence().carriageReturn().build());
		await().atMost(4, TimeUnit.SECONDS).untilAsserted(() -> {
			List<String> lines = session.screen().lines();
			Condition<String> prompt = new Condition<>(line -> line.contains("shell:"), "Shell has expected prompt");
			assertThat(lines).areExactly(3, prompt);
		});

		session.write(session.writeSequence().ctrl('l').build());
		await().atMost(4, TimeUnit.SECONDS).untilAsserted(() -> {
			List<String> lines = session.screen().lines();
			Condition<String> prompt = new Condition<>(line -> line.contains("shell:"), "Shell has expected prompt");
			assertThat(lines).areExactly(1, prompt);
		});

		session.abort();
		ShellScreen screen1 = session.screen();
		ShellScreen screen2 = session.screen();
	}

	@Test
	void testInteractive2() throws Exception {
		InteractiveShellSession session = client.interactive().run();

		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			ShellAssertions.assertThat(session.screen()).containsText("shell:");
		});

		session.write(session.writeSequence().text("xxx").build());
		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			ShellAssertions.assertThat(session.screen()).containsText("xxx");
		});

		session.abort();
		ShellScreen screen1 = client.screen();
		session.abort();
		ShellScreen screen2 = client.screen();
	}

	@Test
	void testNonInteractive() throws Exception {
		Condition<String> helpCondition = new Condition<>(line -> line.contains("AVAILABLE COMMANDS"),
				"Help has expected output");

		Condition<String> helpHelpCondition = new Condition<>(line -> line.contains("help - Display help about available commands"),
				"Help help has expected output");

		// ShellScreen screen1 = client.screen();
		NonInteractiveShellSession session1 = client.nonInterative("help").run();

		// ShellScreen screen2 = client.screen();
		// ShellScreen screen3 = client.screen();

		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			// ShellAssertions.assertThat(session1.screen()).containsText("AVAILABLE COMMANDS");
			List<String> lines = session1.screen().lines();
			assertThat(lines).areExactly(1, helpCondition);
			assertThat(lines).areNot(helpHelpCondition);
		});

		session1.write(session1.writeSequence().clearScreen().build());
		NonInteractiveShellSession session2 = client.nonInterative("help help").run();
		// client.nonInterative("help", "help").run();
		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			// ShellAssertions.assertThat(session2.screen()).containsText("Display help about available commands");
			List<String> lines = session2.screen().lines();
			assertThat(lines).areNot(helpCondition);
			assertThat(lines).areExactly(1, helpHelpCondition);
		});
	}

}
