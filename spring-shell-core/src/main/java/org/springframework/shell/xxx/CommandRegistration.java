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

import java.lang.reflect.Type;
import java.util.function.Function;

public interface CommandRegistration {

	String getCommand();
	String getHelp();
	String getDescription();
	Function<CommandExecutionContext, ?> getFunction();

	public static Builder builder() {
		return new DefaultBuilder();
	}

	public interface OptionSpec {
		OptionSpec name(String name);
		OptionSpec description(String description);
		Builder and();
	}

	public interface ActionSpec {
		ActionSpec function(Function<CommandExecutionContext, ?> function);
		ActionSpec method(Object bean, String method);
		Builder and();
	}

	public interface Builder {
		Builder type(Type type);
		Builder command(String help);
		Builder help(String help);
		OptionSpec withOption();
		ActionSpec withAction();
		CommandRegistration build();
	}

	static class DefaultOptionSpec implements OptionSpec {

		private BaseBuilder builder;
		private String description;

		DefaultOptionSpec(BaseBuilder builder) {
			this.builder = builder;
		}

		@Override
		public OptionSpec name(String name) {
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

		public DefaultCommandRegistration(String command, String help, String description,
				Function<CommandExecutionContext, ?> function) {
			this.command = command;
			this.help = help;
			this.description = description;
			this.function = function;
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
	}

	static class DefaultBuilder extends BaseBuilder {

	}

	static class BaseBuilder implements Builder {

		private String command;
		private String help;
		private String description;
		private Function<CommandExecutionContext, ?> function;

		@Override
		public Builder type(Type type) {
			return this;
		}

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
			return new DefaultOptionSpec(this);
		}

		@Override
		public ActionSpec withAction() {
			return new DefaultActionSpec(this);
		}

		@Override
		public CommandRegistration build() {
			return new DefaultCommandRegistration(command, help, description, function);
		}
	}

}
