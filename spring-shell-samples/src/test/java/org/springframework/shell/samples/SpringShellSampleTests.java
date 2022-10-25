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

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.shell.samples.standard.ResolvedCommands;
import org.springframework.shell.test.ShellAssertions;
import org.springframework.shell.test.ShellClient;
import org.springframework.shell.test.autoconfigure.ShellTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@ShellTest
@Import(ResolvedCommands.ResolvedCommandsConfiguration.class)
public class SpringShellSampleTests {

	@Autowired
	ShellClient.Builder builder;

	@Test
	void componentSingle() {
		ShellClient client = builder.build();
		client.shell();

		client.write(client.writeSequence().command("component single").build());
		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			ShellAssertions.assertThat(client.screen()).containsText("❯ key1");
		});

		client.write(client.writeSequence().keyDown().build());
		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			ShellAssertions.assertThat(client.screen()).containsText("❯ key2");
		});

		client.write(client.writeSequence().cr().build());
		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			ShellAssertions.assertThat(client.screen()).containsText("Got value value2");
		});
	}

	@Test
	void componentMulti() {
		ShellClient client = builder.build();
		client.shell();

		client.write(client.writeSequence().command("component multi").build());
		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			ShellAssertions.assertThat(client.screen()).containsText("❯ ☐  key1");
		});

		client.write(client.writeSequence().space().build());
		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			ShellAssertions.assertThat(client.screen()).containsText("❯ ☒  key1");
		});

		client.write(client.writeSequence().cr().build());
		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			ShellAssertions.assertThat(client.screen()).containsText("Got value value1,value2");
		});
	}

	@Test
	void componentPathSearch() {
		ShellClient client = builder.build();
		client.shell();

		client.write(client.writeSequence().command("component path search").build());
		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			ShellAssertions.assertThat(client.screen()).containsText("<path> <pattern>");
			assertThat(client.screen()).asInstanceOf(ShellAssertions.SHELLSCREEN).containsText("<path> <pattern>");

		});

		// client.write(client.writeSequence().text(". src").build());
		// await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
		// 	assertThat(client.screen()).contains("src");
		// });
	}
}
