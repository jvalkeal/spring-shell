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

import java.util.Optional;

import org.springframework.shell.xxx.CommandParser.Result;
import org.springframework.shell.xxx.CommandParser.Results;

/**
 * Interface containing information about current command execution.
 *
 * @author Janne Valkealahti
 */
public interface CommandContext {

	/**
	 * Gets a raw args passed into a currently executing command.
	 *
	 * @return raw command arguments
	 */
	String[] getArgs();

	boolean hasMappedOption(String name);
	Results getParseResults();
	<T> T getOptionValue(String name);

	static CommandContext of(String[] args, Results results) {
		return new DefaultCommandContext(args, results);
	}

	/**
	 * Default implementation of a {@link CommandContext}.
	 */
	static class DefaultCommandContext implements CommandContext {

		private String[] args;
		private Results results;

		DefaultCommandContext(String[] args, Results results) {
			this.args = args;
			this.results = results;
		}

		@Override
		public String[] getArgs() {
			return args;
		}

		@Override
		public boolean hasMappedOption(String name) {
			return find(name).isPresent();
		}

		@Override
		public Results getParseResults() {
			return results;
		}

		@Override
		public <T> T getOptionValue(String name) {
			Optional<Result> find = find(name);
			if (find.isPresent()) {
				return (T) find.get().value();
			}
			return null;
		}

		private Optional<Result> find(String name) {
			return results.results().stream()
				.filter(r -> r.option().getName().equals(name))
				.findFirst();
		}
	}
}
