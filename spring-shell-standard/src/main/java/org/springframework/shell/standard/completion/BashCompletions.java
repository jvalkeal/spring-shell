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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.core.io.ResourceLoader;
import org.springframework.shell.CommandRegistry;
import org.springframework.shell.ParameterResolver;

/**
 * Completion script generator for a {@code bash}.
 *
 * @author Janne Valkealahti
 */
public class BashCompletions extends AbstractCompletions {

	public BashCompletions(ResourceLoader resourceLoader, CommandRegistry commandRegistry,
			List<ParameterResolver> parameterResolvers) {
		super(resourceLoader, commandRegistry, parameterResolvers);
	}

	public String generate(String rootCommand) {
		List<CommandModelCommand> topcommands = generateCommandModel().commands().stream()
				.collect(Collectors.toList());
		List<CommandModelCommand> subcommands = topcommands.stream()
				// .flatMap(c -> c.subCommands().stream())
				.flatMap(c -> flatten(c))
				.collect(Collectors.toList());
		return builder()
				.withDefaultAttribute("name", rootCommand)
				.withDefaultMultiAttribute("topcommands", topcommands)
				.withDefaultMultiAttribute("subcommands", subcommands)
				.appendResourceWithRender("classpath:completion/bash/pre-template.st")
				.appendResourceWithRender("classpath:completion/bash/command-template.st")
				.appendResourceWithRender("classpath:completion/bash/root-template.st")
				.appendResourceWithRender("classpath:completion/bash/post-template.st")
				.build();
	}

	private Stream<CommandModelCommand> flatten(CommandModelCommand command) {
		return Stream.concat(Stream.of(command), command.subCommands().stream().flatMap(c -> flatten(c)));
	}
}
