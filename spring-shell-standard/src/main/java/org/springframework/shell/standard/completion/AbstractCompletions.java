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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.stringtemplate.v4.ST;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.shell.CommandRegistry;
import org.springframework.shell.MethodTarget;
import org.springframework.shell.ParameterDescription;
import org.springframework.shell.ParameterResolver;
import org.springframework.shell.Utils;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Base class for completion script commands providing functionality for
 * resource handling and templating with {@code antrl stringtemplate}.
 *
 * @author Janne Valkealahti
 */
public abstract class AbstractCompletions {

	private final ResourceLoader resourceLoader;
	private final CommandRegistry commandRegistry;
	private final List<ParameterResolver> parameterResolvers;

	public AbstractCompletions(ResourceLoader resourceLoader, CommandRegistry commandRegistry,
			List<ParameterResolver> parameterResolvers) {
		this.resourceLoader = resourceLoader;
		this.commandRegistry = commandRegistry;
		this.parameterResolvers = parameterResolvers;
	}

	protected Builder builder() {
		return new DefaultBuilder();
	}

	/**
	 * Generates a model for a recursive command model starting from root
	 * level going down with all sub commands with options. Essentially providing
	 * all needed to build completions structure.
	 */
	protected CommandModel generateCommandModel() {
		Map<String, MethodTarget> commandsByName = commandRegistry.listCommands();
		HashMap<String, DefaultCommandModelCommand> commands = new HashMap<>();
		List<CommandModelCommand> topCommands = new ArrayList<>();
		commandsByName.entrySet().stream()
			.forEach(entry -> {
				String key = entry.getKey();
				String[] splitKeys = key.split(" ");
				String commandKey = "";
				for (int i = 0; i < splitKeys.length; i++) {
					DefaultCommandModelCommand parent = null;
					if (i > 0) {
						parent = commands.get(commandKey);
						commandKey = commandKey + " " + splitKeys[i];
					}
					else {
						commandKey = splitKeys[i];
					}
					DefaultCommandModelCommand command = commands.computeIfAbsent(commandKey,
							(fullCommand) -> new DefaultCommandModelCommand(fullCommand));
					MethodTarget methodTarget = entry.getValue();
					List<ParameterDescription> parameterDescriptions = getParameterDescriptions(methodTarget);
					List<DefaultCommandModelOption> options = parameterDescriptions.stream()
							.flatMap(pd -> pd.keys().stream())
							.map(k -> new DefaultCommandModelOption(k))
							.collect(Collectors.toList());
					command.addOptions(options);
					if (parent != null) {
						parent.addCommand(command);
					}
					if (i == 0) {
						topCommands.add(command);
					}
				}
			});
		return new DefaultCommandModel(topCommands);
	}

	private List<ParameterDescription> getParameterDescriptions(MethodTarget methodTarget) {
		return Utils.createMethodParameters(methodTarget.getMethod())
				.flatMap(mp -> parameterResolvers.stream().filter(pr -> pr.supports(mp)).limit(1L)
						.flatMap(pr -> pr.describe(mp)))
				.collect(Collectors.toList());
	}

	interface CommandModel {
		List<CommandModelCommand> commands();
	}

	interface CommandModelCommand  {
		List<String> getParts();
		String getLast();
		boolean hasSubCommands();
		List<CommandModelCommand> subCommands();
		List<CommandModelOption> options();
	}

	interface CommandModelOption {
		String option();
	}

	class DefaultCommandModel implements CommandModel {

		private final List<CommandModelCommand> commands;

		public DefaultCommandModel(List<CommandModelCommand> commands) {
			this.commands = commands;
		}

		@Override
		public List<CommandModelCommand> commands() {
			return commands;
		}
	}

	class DefaultCommandModelCommand implements CommandModelCommand {

		private String fullCommand;
		private List<CommandModelCommand> commands = new ArrayList<>();
		private List<CommandModelOption> options = new ArrayList<>();

		DefaultCommandModelCommand(String fullCommand) {
			this.fullCommand = fullCommand;
		}

		@Override
		public List<String> getParts() {
			return Arrays.asList(fullCommand.split(" "));
		}

		@Override
		public String getLast() {
			String[] split = fullCommand.split(" ");
			return split[split.length - 1];
		}

		@Override
		public boolean hasSubCommands() {
			return !commands.isEmpty();
		}

		@Override
		public List<CommandModelCommand> subCommands() {
			return commands;
		}

		@Override
		public List<CommandModelOption> options() {
			return options;
		}

		void addOptions(List<DefaultCommandModelOption> options) {
			this.options.addAll(options);
		}

		void addCommand(DefaultCommandModelCommand command) {
			commands.add(command);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getEnclosingInstance().hashCode();
			result = prime * result + ((fullCommand == null) ? 0 : fullCommand.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			DefaultCommandModelCommand other = (DefaultCommandModelCommand) obj;
			if (!getEnclosingInstance().equals(other.getEnclosingInstance())) {
				return false;
			}
			if (fullCommand == null) {
				if (other.fullCommand != null) {
					return false;
				}
			} else if (!fullCommand.equals(other.fullCommand)) {
				return false;
			}
			return true;
		}

		private AbstractCompletions getEnclosingInstance() {
			return AbstractCompletions.this;
		}
	}

	class DefaultCommandModelOption implements CommandModelOption {

		private String option;

		public DefaultCommandModelOption(String option) {
			this.option = option;
		}

		@Override
		public String option() {
			return option;
		}
	}

	private static String resourceAsString(Resource resource) {
		try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
			return FileCopyUtils.copyToString(reader);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	interface Builder {

		Builder withDefaultAttribute(String name, Object value);
		Builder withDefaultMultiAttribute(String name, List<? extends Object> values);
		Builder appendResourceWithRender(String resource);
		String build();
	}

	class DefaultBuilder implements Builder {

		private final MultiValueMap<String, Object> defaultAttributes = new LinkedMultiValueMap<>();
		private final List<Supplier<String>> operations = new ArrayList<>();

		@Override
		public Builder withDefaultAttribute(String name, Object value) {
			this.defaultAttributes.add(name, value);
			return this;
		}

		@Override
		public Builder withDefaultMultiAttribute(String name, List<? extends Object> values) {
			this.defaultAttributes.addAll(name, values);
			return this;
		}

		@Override
		public Builder appendResourceWithRender(String resource) {
			// delay so that we render with build
			Supplier<String> operation = () -> {
				String template = resourceAsString(resourceLoader.getResource(resource));
				ST st = new ST(template);
				defaultAttributes.entrySet().stream().forEach(entry -> {
					String key = entry.getKey();
					List<Object> values = entry.getValue();
					values.stream().forEach(v -> {
						st.add(key, v);
					});
				});
				return st.render();
			};
			operations.add(operation);
			return this;
		}

		@Override
		public String build() {
			StringBuilder buf = new StringBuilder();
			operations.stream().forEach(operation -> {
				buf.append(operation.get());
			});
			return buf.toString();
		}
	}
}
