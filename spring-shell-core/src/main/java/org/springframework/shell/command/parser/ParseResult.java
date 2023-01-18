/*
 * Copyright 2023 the original author or authors.
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
package org.springframework.shell.command.parser;

import java.util.List;

import org.springframework.shell.command.CommandOption;
import org.springframework.shell.command.CommandRegistration;

public class ParseResult {

	private CommandRegistration commandRegistration;
	private List<OptionResult> optionResults;
	private String directive;

	ParseResult(CommandRegistration commandRegistration, List<OptionResult> optionResults, String directive) {
		this.commandRegistration = commandRegistration;
		this.optionResults = optionResults;
		this.directive = directive;
	}

	public CommandRegistration getCommandRegistration() {
		return commandRegistration;
	}

	public List<OptionResult> getOptionResults() {
		return optionResults;
	}

	public String getDirective() {
		return directive;
	}

	public static class OptionResult {
		private CommandOption option;
		private Object value;
		public OptionResult(CommandOption option, Object value) {
			this.option = option;
			this.value = value;
		}

		public CommandOption getOption() {
			return option;
		}

		public Object getValue() {
			return value;
		}
	}
}
