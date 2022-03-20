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
package org.springframework.shell.command;

import org.junit.jupiter.api.Test;

import org.springframework.shell.command.CommandCatalog;
import org.springframework.shell.command.CommandRegistration;

import static org.assertj.core.api.Assertions.assertThat;

public class CommandCatalogTests extends AbstractCommandTests {

	@Test
	public void testCommandCatalog () {
		CommandRegistration r1 = CommandRegistration.builder()
			.command("group1 sub1")
			.targetFunction()
				.function(function1)
				.and()
			.build();
		CommandCatalog catalog = CommandCatalog.of();
		catalog.register(r1);
		assertThat(catalog.getCommands()).hasSize(1);
		catalog.unregister(r1);
		assertThat(catalog.getCommands()).hasSize(0);
	}
}
