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

import com.jediterm.terminal.ui.TerminalSession;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.shell.Shell;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.shell.test.ShellClient;
import org.springframework.shell.test.ShellClient.Builder;

/**
 *
 * @author Janne Valkealahti
 */
@AutoConfiguration
public class ShellClientAutoConfiguration {

	@Bean
	@Scope("prototype")
	@ConditionalOnMissingBean
	ShellClient.Builder shellClientBuilder(TerminalSession widget, Shell shell, PromptProvider promptProvider,
			LineReader lineReader, Terminal terminal) {
		Builder builder = ShellClient.builder();
		builder.xxx(widget, shell, promptProvider, lineReader, terminal);
		return builder;
	}
}