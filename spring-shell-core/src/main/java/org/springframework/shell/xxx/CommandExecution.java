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
import java.util.stream.Stream;

import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.support.HeaderMethodArgumentResolver;
import org.springframework.messaging.handler.annotation.support.HeadersMethodArgumentResolver;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolverComposite;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.shell.xxx.CommandParser.Results;

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
	 * @return default command execution
	 */
	public static CommandExecution of() {
		return new DefaultCommandExecution();
	}

	/**
	 * Default implementation of a {@link CommandExecution}.
	 */
	static class DefaultCommandExecution implements CommandExecution {

		public Object evaluate(CommandRegistration registration, String[] args) {

			List<CommandOption> options = registration.getOptions();
			CommandParser parser = CommandParser.of();
			Results results = parser.parse(options, args);

			CommandContext ctx = CommandContext.of(args, results);

			Object res = null;

			Function<CommandContext, ?> function = registration.getFunction();
			InvocableHandlerMethod invocableHandlerMethod = registration.getMethod();
			if (function != null) {
				res = function.apply(ctx);
			}
			else if (invocableHandlerMethod != null) {
				HandlerMethodArgumentResolverComposite argumentResolvers = new HandlerMethodArgumentResolverComposite();
				argumentResolvers.addResolver(new HeaderMethodArgumentResolver(new DefaultConversionService(), null));
				argumentResolvers.addResolver(new HeadersMethodArgumentResolver());
				invocableHandlerMethod.setMessageMethodArgumentResolvers(argumentResolvers);
				try {
					MessageBuilder<String[]> messageBuilder = MessageBuilder.withPayload(args);
					results.results().stream().forEach(r -> {
						for (String alias : r.option().getAliases()) {
							messageBuilder.setHeader(alias, r.value());
						}
					});
					// Message<String[]> build = messageBuilder.build();
					// res = invocableHandlerMethod.invoke(messageBuilder.build(), args);
					res = invocableHandlerMethod.invoke(messageBuilder.build(), null);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			return res;
		}
	}
}
