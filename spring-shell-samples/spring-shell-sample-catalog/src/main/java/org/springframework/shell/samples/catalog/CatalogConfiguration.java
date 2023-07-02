/*
 * Copyright 2023 the original author or authors.
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
package org.springframework.shell.samples.catalog;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.boot.NonInteractiveShellRunnerCustomizer;
import org.springframework.shell.command.annotation.CommandScan;
import org.springframework.util.StringUtils;

@Configuration
@CommandScan
class CatalogConfiguration {

    private static final String SINGLE_QUOTE = "\'";
    private static final String DOUBLE_QUOTE = "\"";

	private static boolean isQuoted(String str) {
		if (str == null) {
			return false;
		}
		return str.startsWith(SINGLE_QUOTE) && str.endsWith(SINGLE_QUOTE)
				|| str.startsWith(DOUBLE_QUOTE) && str.endsWith(DOUBLE_QUOTE);
	}

	private Function<ApplicationArguments, List<String>> commandsFromInputArgs = args -> {
		if (args.getSourceArgs().length == 0) {
			Collections.singletonList("catalog");
		}
		// re-quote if needed having whitespace
		String raw = Arrays.stream(args.getSourceArgs())
			.map(a -> {
				if (!isQuoted(a) && StringUtils.containsWhitespace(a)) {
					return "\"" + a + "\"";
				}
				return a;
			})
			.collect(Collectors.joining(" "));
		return Collections.singletonList("catalog " + raw);
	};

	@Bean
	public NonInteractiveShellRunnerCustomizer runnerCustomizer() {
		return runner -> {
			runner.setCommandsFromInputArgs(commandsFromInputArgs);
		};
	}

}
