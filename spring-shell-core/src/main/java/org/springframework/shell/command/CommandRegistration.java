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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * Interface defining a command registration endpoint.
 *
 * @author Janne Valkealahti
 */
public interface CommandRegistration {

	/**
	 * Gets an array of commands for this registration.
	 *
	 * @return array of commands
	 */
	String[] getCommands();

	/**
	 * Get help for a command.
	 *
	 * @return the help
	 */
	String getHelp();

	/**
	 * Get description for a command.
	 *
	 * @return the description
	 */
	String getDescription();

	/**
	 * Gets a target {@link Function} to execute.
	 *
	 * @return the target function to execute
	 */
	@Nullable
	Function<CommandContext, ?> getFunction();

	/**
	 * Gets a target {@link InvocableHandlerMethod} to execute.
	 *
	 * @return the target method to execute
	 */
	@Nullable
	InvocableHandlerMethod getMethod();

	/**
	 * Gets an options.
	 *
	 * @return the options
	 */
	List<CommandOption> getOptions();

	/**
	 * Gets a new instance of a {@link Buidler}.
	 *
	 * @return a new builder instance
	 */
	public static Builder builder() {
		return new DefaultBuilder();
	}

	public interface OptionSpec {

		/**
		 * Define long option names.
		 *
		 * @param names the long option names
		 * @return option spec for chaining
		 */
		OptionSpec longNames(String... names);

		/**
		 * Define short option names.
		 *
		 * @param names the long option names
		 * @return option spec for chaining
		 */
		OptionSpec shortNames(Character... names);

		/**
		 * Define a {@code description} for an option.
		 *
		 * @param description the option description
		 * @return option spec for chaining
		 */
		OptionSpec description(String description);

		/**
		 * Return a builder for chaining.
		 *
		 * @return a builder for chaining
		 */
		Builder and();
	}

	public interface TargetFunctionSpec {
		TargetFunctionSpec function(Function<CommandContext, ?> function);
		Builder and();
	}

	public interface TargetMethodSpec {
		TargetMethodSpec method(Object bean, String method, @Nullable Class<?>... paramTypes);
		Builder and();
	}

	public interface Builder {

		/**
		 * Define commands this registration uses. Essentially defines a full set of
		 * main and sub commands. It doesn't matter if full command is defined in one
		 * string or multiple strings as "words" are splitted and trimmed with
		 * whitespaces. You will get result of {@code command subcommand1 subcommand2, ...}.
		 *
		 * @param commands the commands
		 * @return builder for chaining
		 */
		Builder command(String... commands);

		/**
		 * Define a simple help text for a commmand.
		 *
		 * @param help the help text
		 * @return builder for chaining
		 */
		Builder help(String help);

		/**
		 * Define an option what this command should user for. Can be used multiple
		 * times.
		 *
		 * @return option spec for chaining
		 */
		OptionSpec withOption();

		/**
		 * Define a target what this command should execute. Can be used only once.
		 *
		 * @return action spec for chaining
		 */
		TargetFunctionSpec targetFunction();

		/**
		 * Define a target what this command should execute. Can be used only once.
		 *
		 * @return action spec for chaining
		 */
		TargetMethodSpec targetMethod();

		/**
		 * Builds a {@link CommandRegistration}.
		 *
		 * @return a command registration
		 */
		CommandRegistration build();
	}

	static class DefaultOptionSpec implements OptionSpec {

		private BaseBuilder builder;
		private String[] longNames;
		private Character[] shortNames;
		private String description;

		DefaultOptionSpec(BaseBuilder builder) {
			this.builder = builder;
		}

		@Override
		public OptionSpec longNames(String... names) {
			this.longNames = names;
			return this;
		}

		@Override
		public OptionSpec shortNames(Character... names) {
			this.shortNames = names;
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

		public String[] getLongNames() {
			return longNames;
		}

		public Character[] getShortNames() {
			return shortNames;
		}

		public String getDescription() {
			return description;
		}
	}

	static class DefaultTargetFunctionSpec implements TargetFunctionSpec {

		private BaseBuilder builder;
		private Function<CommandContext, ?> function;

		DefaultTargetFunctionSpec(BaseBuilder builder) {
			this.builder = builder;
		}

		@Override
		public TargetFunctionSpec function(Function<CommandContext, ?> function) {
			this.function = function;
			return this;
		}

		@Override
		public Builder and() {
			builder.function = function;
			return builder;
		}
	}

	static class DefaultTargetMethodSpec implements TargetMethodSpec {

		private BaseBuilder builder;
		private Object methodBean;
		private String methodMethod;
		private Class<?>[] paramTypes;

		DefaultTargetMethodSpec(BaseBuilder builder) {
			this.builder = builder;
		}

		@Override
		public TargetMethodSpec method(Object bean, String method, @Nullable Class<?>... paramTypes) {
			this.methodBean = bean;
			this.methodMethod = method;
			this.paramTypes = paramTypes;
			return this;
		}

		@Override
		public Builder and() {
			if (methodBean != null && methodMethod != null) {
				Method method = ReflectionUtils.findMethod(methodBean.getClass(), methodMethod,
						ObjectUtils.isEmpty(paramTypes) ? null : paramTypes);
				InvocableHandlerMethod invocableHandlerMethod = new InvocableHandlerMethod(methodBean, method);
				builder.invocableHandlerMethod = invocableHandlerMethod;
			}
			return builder;
		}
	}

	static class DefaultCommandRegistration implements CommandRegistration {

		private String[] commands;
		private String help;
		private String description;
		private Function<CommandContext, ?> function;
		private InvocableHandlerMethod invocableHandlerMethod;
		private List<DefaultOptionSpec> optionSpecs;

		public DefaultCommandRegistration(String[] commands, String help, String description,
				Function<CommandContext, ?> function, List<DefaultOptionSpec> optionSpecs, InvocableHandlerMethod invocableHandlerMethod) {
			this.commands = commands;
			this.help = help;
			this.description = description;
			this.function = function;
			this.optionSpecs = optionSpecs;
			this.invocableHandlerMethod = invocableHandlerMethod;
		}

		@Override
		public String[] getCommands() {
			return commands;
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
		public Function<CommandContext, ?> getFunction() {
			return function;
		}

		@Override
		public InvocableHandlerMethod getMethod() {
			return invocableHandlerMethod;
		}

		@Override
		public List<CommandOption> getOptions() {
			return optionSpecs.stream()
				.map(o -> CommandOption.of(o.getLongNames(), o.getShortNames(), o.getDescription()))
				.collect(Collectors.toList());
		}
	}

	static class DefaultBuilder extends BaseBuilder {

	}

	static class BaseBuilder implements Builder {

		private String[] commands;
		private String help;
		private String description;
		private Function<CommandContext, ?> function;
		private InvocableHandlerMethod invocableHandlerMethod;
		private List<DefaultOptionSpec> optionSpecs = new ArrayList<>();

		@Override
		public Builder command(String... commands) {
			Assert.notNull(commands, "commands must be set");
			this.commands = Arrays.asList(commands).stream()
				.flatMap(c -> Stream.of(c.split(" ")))
				.filter(c -> StringUtils.hasText(c))
				.map(c -> c.trim())
				.collect(Collectors.toList())
				.toArray(new String[0]);
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
		public TargetFunctionSpec targetFunction() {
			return new DefaultTargetFunctionSpec(this);
		}

		@Override
		public TargetMethodSpec targetMethod() {
			return new DefaultTargetMethodSpec(this);
		}

		@Override
		public CommandRegistration build() {
			Assert.notNull(commands, "command cannot be empty");
			int targets = 0;
			if (function != null) {
				targets++;
			}
			if (invocableHandlerMethod != null) {
				targets++;
			}
			Assert.isTrue(targets == 1, "only one target can exist");
			return new DefaultCommandRegistration(commands, help, description, function, optionSpecs, invocableHandlerMethod);
		}
	}
}
