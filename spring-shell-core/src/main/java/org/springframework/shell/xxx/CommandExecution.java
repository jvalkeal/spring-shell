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

import java.util.function.Function;

public class CommandExecution {

	public Object evaluate(CommandRegistration registration, String[] args) {
		Function<CommandExecutionContext, ?> function = registration.getFunction();
		CommandExecutionContext ctx = CommandExecutionContext.of();
		Object res = function.apply(ctx);
		return res;
	}

	private CommandExecutionContext resolveContext(CommandRegistration registration, String[] args) {
		CommandExecutionContext ctx = CommandExecutionContext.of();
		return ctx;
	}
}
