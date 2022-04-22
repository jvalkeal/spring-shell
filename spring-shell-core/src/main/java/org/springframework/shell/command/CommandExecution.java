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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.shell.command.CommandParser.Results;
import org.springframework.shell.command.CommandRegistration.TargetInfo;
import org.springframework.shell.command.CommandRegistration.TargetInfo.TargetType;
import org.springframework.shell.command.invocation.InvocableShellMethod;
import org.springframework.shell.command.invocation.ShellMethodArgumentResolverComposite;

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
	 * @param args         the command args
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

			TargetInfo targetInfo = registration.getTarget();

			// pick the target to execute
			if (targetInfo.getTargetType() == TargetType.FUNCTION) {
				res = targetInfo.getFunction().apply(ctx);
			}
			else if (targetInfo.getTargetType() == TargetType.METHOD) {
				try {
					MessageBuilder<String[]> messageBuilder = MessageBuilder.withPayload(args);
					Map<String, Object> paramValues = new HashMap<>();
					results.results().stream().forEach(r -> {
						if (r.option().getLongNames() != null) {
							for (String n : r.option().getLongNames()) {
								messageBuilder.setHeader(n, r.value());
								paramValues.put(n, r.value());
							}
						}
						if (r.option().getShortNames() != null) {
							for (Character n : r.option().getShortNames()) {
								messageBuilder.setHeader(n.toString(), r.value());
							}
						}
					});
					messageBuilder.setHeader(CommandContextMethodArgumentResolver.HEADER_COMMAND_CONTEXT, ctx);

					InvocableShellMethod invocableShellMethod = new InvocableShellMethod(targetInfo.getBean(), targetInfo.getMethod());
					ShellMethodArgumentResolverComposite argumentResolvers = new ShellMethodArgumentResolverComposite();
					if (resolvers != null) {
						argumentResolvers.addResolvers(resolvers);
					}
					if (!paramValues.isEmpty()) {
						argumentResolvers.addResolver(new ParamNameHandlerMethodArgumentResolver(paramValues));
					}
					invocableShellMethod.setMessageMethodArgumentResolvers(argumentResolvers);

					res = invocableShellMethod.invoke(messageBuilder.build(), results.positional().toArray());

				} catch (Exception e) {
					throw new CommandExecutionException(e);
				}
			}

			return res;
		}
	}

	@Order(100)
	static class ParamNameHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

		private final Map<String, Object> paramValues = new HashMap<>();
		ConversionService conversionService = new DefaultConversionService();

		ParamNameHandlerMethodArgumentResolver(Map<String, Object> paramValues) {
			this.paramValues.putAll(paramValues);
		}

		@Override
		public boolean supportsParameter(MethodParameter parameter) {
			String parameterName = parameter.getParameterName();
			if (parameterName == null) {
				return false;
			}
			return paramValues.containsKey(parameterName) && conversionService
					.canConvert(paramValues.get(parameterName).getClass(), parameter.getParameterType());
		}

		@Override
		public Object resolveArgument(MethodParameter parameter, Message<?> message) throws Exception {
			return conversionService.convert(paramValues.get(parameter.getParameterName()), parameter.getParameterType());
		}

	}

	static class DelegatingInvocableHandlerMethod extends InvocableHandlerMethod {

		public DelegatingInvocableHandlerMethod(Object bean, Method method) {
			super(bean, method);
		}

		@Override
		public Object invoke(Message<?> message, Object... providedArgs) throws Exception {
			if (providedArgs == null) {
				return super.invoke(message, providedArgs);
			}
			else {
				return super.doInvoke(providedArgs);
			}
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
