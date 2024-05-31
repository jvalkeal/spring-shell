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
package org.springframework.shell.jline;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import org.springframework.boot.ApplicationArguments;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.Input;
import org.springframework.shell.InputProvider;
import org.springframework.shell.Shell;
import org.springframework.shell.ShellRunner;
import org.springframework.shell.context.InteractionMode;
import org.springframework.shell.context.ShellContext;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * {@link ShellRunner} handling fallback if none other runners are able to
 * handle user interaction. Default behaviour is to run {@code help} command
 * and can be changed to something else like having a single command
 * if all other runners are disabled.
 *
 * @author Janne Valkealahti
 */
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class FallbackShellRunner implements ShellRunner {

	private final Shell shell;
	private final ShellContext shellContext;
	private String command;
	private boolean append;

	public record ArgsContext(String[] args, String command, boolean append){}

	private static Function<ArgsContext, String> DEFAULT_COMMAND_FACTORY = ctx -> {
		Builder<String> builder = Stream.builder();
		builder.add(ctx.command);
		if (ctx.append) {
			for (String arg : ctx.args) {
				builder.add(arg);
			}
		}
		return builder.build().collect(Collectors.joining(" "));
	};

	private Function<ArgsContext, String> commandFactory = DEFAULT_COMMAND_FACTORY;

	public FallbackShellRunner(Shell shell, ShellContext shellContext, String command) {
		Assert.state(StringUtils.hasText(command), "command must be set");
		this.shell = shell;
		this.shellContext = shellContext;
		this.command = command;
	}

	@Override
	public boolean canRun(ApplicationArguments args) {
		return true;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		shellContext.setInteractionMode(InteractionMode.NONINTERACTIVE);
		ArgsContext ctx = new ArgsContext(args.getSourceArgs(), command, append);
		String generated = this.commandFactory.apply(ctx);
		SingleInputProvider inputProvider = new SingleInputProvider(generated);
		shell.run(inputProvider);
	}

	private static class SingleInputProvider implements InputProvider {

		final String command;
		boolean done = false;

		SingleInputProvider(String command) {
			this.command = command;
		}

		@Override
		public Input readInput() {
			if (!done) {
				done = true;
				return new SingleInput(command);
			}
			return null;
		}
	}

	private static class SingleInput implements Input {

		final String command;

		SingleInput(String command) {
			this.command = command;
		}

		@Override
		public String rawText() {
			return this.command;
		}
	}

}
