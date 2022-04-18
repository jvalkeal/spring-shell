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

import java.util.List;
import java.util.function.Function;

import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolverComposite;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.shell.command.CommandParser.Results;

/**
 * Interface to evaluate a result from a command with an arguments.
 *
 * @author Janne Valkealahti
 */
public interface CommandExecution {

	/**
	 * Evaluate a command with a given arguments.
	 *
	 * @param registration the command registration
	 * @param args the command args
	 * @return evaluated execution
	 */
	Object evaluate(CommandRegistration registration, String[] args);

	/**
	 * Gets an instance of a default {@link CommandExecution}.
	 *
	 * @param resolvers the handler method argument resolvers
	 * @return default command execution
	 */
	public static CommandExecution of(List<? extends HandlerMethodArgumentResolver> resolvers) {
		return new DefaultCommandExecution(resolvers);
	}

	/**
	 * Default implementation of a {@link CommandExecution}.
	 */
	static class DefaultCommandExecution implements CommandExecution {

		private List<? extends HandlerMethodArgumentResolver> resolvers;

		public DefaultCommandExecution(List<? extends HandlerMethodArgumentResolver> resolvers) {
			this.resolvers = resolvers;
		}

		public Object evaluate(CommandRegistration registration, String[] args) {

			List<CommandOption> options = registration.getOptions();
			CommandParser parser = CommandParser.of();
			Results results = parser.parse(options, args);

			CommandContext ctx = CommandContext.of(args, results);

			Object res = null;

			Function<CommandContext, ?> function = registration.getFunction();
			InvocableHandlerMethod invocableHandlerMethod = registration.getMethod();

			// pick the target to execute
			if (function != null) {
				res = function.apply(ctx);
			}
			else if (invocableHandlerMethod != null) {
				HandlerMethodArgumentResolverComposite argumentResolvers = new HandlerMethodArgumentResolverComposite();
				if (resolvers != null) {
					argumentResolvers.addResolvers(resolvers);
				}
				invocableHandlerMethod.setMessageMethodArgumentResolvers(argumentResolvers);
				try {
					MessageBuilder<String[]> messageBuilder = MessageBuilder.withPayload(args);
					results.results().stream().forEach(r -> {
						if (r.option().getLongNames() != null) {
							for (String n : r.option().getLongNames()) {
								messageBuilder.setHeader(n, r.value());
							}
						}
						if (r.option().getShortNames() != null) {
							for (Character n : r.option().getShortNames()) {
								messageBuilder.setHeader(n.toString(), r.value());
							}
						}
					});
					messageBuilder.setHeader(CommandContextMethodArgumentResolver.HEADER_COMMAND_CONTEXT, ctx);
					res = invocableHandlerMethod.invoke(messageBuilder.build(), results.positional().toArray());
				} catch (Exception e) {
					throw new CommandExecutionException(e);
				}
			}

			return res;
		}
	}

	static class CommandExecutionException extends RuntimeException {

		public CommandExecutionException(Throwable cause) {
			super(cause);
		}
	}

	static class CommandExecutionHandlerMethodArgumentResolvers {

		private final List<? extends HandlerMethodArgumentResolver> resolvers;

		public CommandExecutionHandlerMethodArgumentResolvers(List<? extends HandlerMethodArgumentResolver> resolvers) {
			this.resolvers = resolvers;
		}

		public List<? extends HandlerMethodArgumentResolver> getResolvers() {
			return resolvers;
		}
	}
}
