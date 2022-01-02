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
package org.springframework.shell.standard.completion;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

public class BashCompletionsTests {

	AnnotationConfigApplicationContext context;

	@BeforeEach
	public void setup() {
		context = new AnnotationConfigApplicationContext();
		context.refresh();
	}

	@AfterEach
	public void clean() {
		if (context != null) {
			context.close();
		}
		context = null;
	}

	@Test
	public void test() {
		BashCompletions generate = new BashCompletions(context);
		List<String> commands = Arrays.asList("command1", "command2");
		String bash = generate.generate("root-command", commands);
		// System.out.println(bash);
		assertThat(bash).contains("root-command");
	}
}
