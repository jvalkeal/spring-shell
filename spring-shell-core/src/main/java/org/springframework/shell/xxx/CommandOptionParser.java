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
import java.util.Optional;

import org.springframework.core.ResolvableType;

/**
 *
 *
 * @author Janne Valkealahti
 */
public interface CommandOptionParser {

	interface Result {
		CommandOption option();
		Object value();

		public static Result of(CommandOption option, Object value) {
			return new DefaultResult(option, value);
		}
	}

	interface Results {
		List<Result> results();

		public static Results of(List<Result> results) {
			return new DefaultResults(results);
		}
	}

	Results parse(List<CommandOption> options, String[] args);

	public static CommandOptionParser of() {
		return new DefaultCommadOptionParser();
	}

	static class DefaultResults implements Results {

		private List<Result> results;

		DefaultResults(List<Result> results) {
			this.results = results;
		}

		@Override
		public List<Result> results() {
			return results;
		}
	}

	static class DefaultResult implements Result {

		private CommandOption option;
		private Object value;

		DefaultResult(CommandOption option, Object value) {
			this.option = option;
			this.value = value;
		}

		@Override
		public CommandOption option() {
			return option;
		}

		@Override
		public Object value() {
			return value;
		}
	}

	static class DefaultCommadOptionParser implements CommandOptionParser {

		@Override
		public Results parse(List<CommandOption> options, String[] args) {

			List<Result> results = new ArrayList<>();

			CommandOption onOption = null;
			for (int i = 0; i < args.length; i++) {
				String arg = args[i];
				if (onOption == null) {
					Optional<CommandOption> option = options.stream().filter(o -> o.getName().equals(arg)).findFirst();
					if (option.isPresent()) {
						onOption = option.get();
					}
				}
				else {
					// ResolvableType type = onOption.getType();
					results.add(Result.of(onOption, arg));
					onOption = null;
				}
			}

			return Results.of(results);
		}

	}
}
