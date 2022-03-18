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
package org.springframework.shell.xxx;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.core.ResolvableType;

/**
 * Interface defining a command registration endpoint.
 *
 * @author Janne Valkealahti
 */
public interface CommandRegistration {

	String getCommand();
	String getHelp();
	String getDescription();
	Function<CommandExecutionContext, ?> getFunction();
	List<CommandOption> getOptions();

	public static Builder builder() {
		return new DefaultBuilder();
	}

	public interface OptionSpec {
		OptionSpec name(String name);
		OptionSpec type(ResolvableType type);
		OptionSpec description(String description);
		Builder and();
	}

	public interface ActionSpec {
		ActionSpec function(Function<CommandExecutionContext, ?> function);
		ActionSpec method(Object bean, String method);
		Builder and();
	}

	public interface Builder {
		Builder command(String help);
		Builder help(String help);
		OptionSpec withOption();
		ActionSpec withAction();
		CommandRegistration build();
	}

	static class DefaultOptionSpec implements OptionSpec {

		private BaseBuilder builder;
		private String name;
		private ResolvableType type;
		private String description;

		DefaultOptionSpec(BaseBuilder builder) {
			this.builder = builder;
		}

		@Override
		public OptionSpec name(String name) {
			this.name = name;
			return this;
		}

		@Override
		public OptionSpec type(ResolvableType type) {
			this.type = type;
			return this;
		}

		@Override
		public OptionSpec description(String description) {
			this.description = description;
			return this;
		}

		@Override
		public Builder and() {
			return builder;
		}

		public String getName() {
			return name;
		}

		public ResolvableType getType() {
			return type;
		}

		public String getDescription() {
			return description;
		}
	}

	static class DefaultActionSpec implements ActionSpec {

		private BaseBuilder builder;
		private Function<CommandExecutionContext, ?> function;

		DefaultActionSpec(BaseBuilder builder) {
			this.builder = builder;
		}

		@Override
		public ActionSpec function(Function<CommandExecutionContext, ?> function) {
			this.function = function;
			return this;
		}

		@Override
		public ActionSpec method(Object bean, String method) {
			return this;
		}

		@Override
		public Builder and() {
			builder.function = function;
			return builder;
		}
	}

	static class DefaultCommandRegistration implements CommandRegistration {

		private String command;
		private String help;
		private String description;
		private Function<CommandExecutionContext, ?> function;
		private List<DefaultOptionSpec> optionSpecs;

		public DefaultCommandRegistration(String command, String help, String description,
				Function<CommandExecutionContext, ?> function, List<DefaultOptionSpec> optionSpecs) {
			this.command = command;
			this.help = help;
			this.description = description;
			this.function = function;
			this.optionSpecs = optionSpecs;
		}

		@Override
		public String getCommand() {
			return command;
		}

		@Override
		public String getHelp() {
			return help;
		}

		@Override
		public String getDescription() {
			return description;
		}

		@Override
		public Function<CommandExecutionContext, ?> getFunction() {
			return function;
		}

		@Override
		public List<CommandOption> getOptions() {
			return optionSpecs.stream()
				.map(o -> CommandOption.of(o.getName(), o.getType(), o.getDescription()))
				.collect(Collectors.toList());
		}
	}

	static class DefaultBuilder extends BaseBuilder {

	}

	static class BaseBuilder implements Builder {

		private String command;
		private String help;
		private String description;
		private Function<CommandExecutionContext, ?> function;
		private List<DefaultOptionSpec> optionSpecs = new ArrayList<>();

		@Override
		public Builder command(String command) {
			this.command = command;
			return this;
		}

		@Override
		public Builder help(String help) {
			this.help = help;
			return this;
		}

		@Override
		public OptionSpec withOption() {
			DefaultOptionSpec spec = new DefaultOptionSpec(this);
			optionSpecs.add(spec);
			return spec;
		}

		@Override
		public ActionSpec withAction() {
			return new DefaultActionSpec(this);
		}

		@Override
		public CommandRegistration build() {
			return new DefaultCommandRegistration(command, help, description, function, optionSpecs);
		}
	}

}
