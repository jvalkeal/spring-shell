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

import java.util.List;
import java.util.function.Function;

import org.springframework.shell.xxx.CommandOptionParser.Results;

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
			CommandOptionParser parser = CommandOptionParser.of();
			Results results = parser.parse(options, args);

			CommandExecutionContext ctx = CommandExecutionContext.of(args, results);

			Function<CommandExecutionContext, ?> function = registration.getFunction();
			Object res = function.apply(ctx);
			return res;
		}
	}
}
