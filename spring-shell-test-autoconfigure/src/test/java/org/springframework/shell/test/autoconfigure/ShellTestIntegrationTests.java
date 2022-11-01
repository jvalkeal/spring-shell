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
import org.springframework.shell.test.autoconfigure.app.ExampleShellApplication;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@ShellTest
@ContextConfiguration(classes = ExampleShellApplication.class)
public class ShellTestIntegrationTests {

	@Autowired
	ShellClient.Builder builder;

	@Test
	void testInteractive1() throws Exception {
		ShellClient client = builder.build();
		client.runInterative();

		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			ShellAssertions.assertThat(client.screen()).containsText("shell");
		});

		client.write("help\n");
		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			ShellAssertions.assertThat(client.screen()).containsText("AVAILABLE COMMANDS");
		});

		client.write(client.writeSequence().carriageReturn().build());
		await().atMost(4, TimeUnit.SECONDS).untilAsserted(() -> {
			List<String> lines = client.screen().lines();
			Condition<String> prompt = new Condition<>(line -> line.contains("shell:"), "prompt");
			assertThat(lines).areExactly(3, prompt);
		});

		client.write(client.writeSequence().clearScreen().build());
		await().atMost(4, TimeUnit.SECONDS).untilAsserted(() -> {
			List<String> lines = client.screen().lines();
			Condition<String> prompt = new Condition<>(line -> line.contains("shell:"), "prompt");
			assertThat(lines).areExactly(1, prompt);
		});
	}

	@Test
	void testInteractive2() throws Exception {
		ShellClient client1 = builder.build();
		client1.runInterative();

		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			ShellAssertions.assertThat(client1.screen()).containsText("shell");
		});

		client1.write(client1.writeSequence().text("xxx").build());
		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			ShellAssertions.assertThat(client1.screen()).containsText("xxx");
		});

		client1.close();

		// ShellClient client2 = builder.build();
		// client2.runInterative();

		// await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
		// 	ShellAssertions.assertThat(client2.screen()).containsText("xxx");
		// });

	}

	@Test
	void testNonInteractive() throws Exception {
		ShellClient client = builder.build();

		client.runNonInterative("help");

		ShellScreen screen1 = client.screen();
		ShellScreen screen2 = client.screen();

		client.write(client.writeSequence().clearScreen().build());

		// client.close();

		client.runNonInterative("help help");
		ShellScreen screen3 = client.screen();
		ShellScreen screen4 = client.screen();

	}

}
