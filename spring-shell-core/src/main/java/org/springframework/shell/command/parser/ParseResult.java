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

/**
 * Results from a {@link Parser} containing needed information like resolved
 * {@link CommandRegistration}, list of {@link CommandOption} instances, errors
 * and directive.
 *
 * @author Janne Valkealahti
 * @see Parser
 */
public class ParseResult {

	private CommandRegistration commandRegistration;
	private List<OptionResult> optionResults;
	private String directive;
	private List<ErrorResult> errorResults;

	ParseResult(CommandRegistration commandRegistration, List<OptionResult> optionResults, String directive,
			List<ErrorResult> errorResults) {
		this.commandRegistration = commandRegistration;
		this.optionResults = optionResults;
		this.directive = directive;
		this.errorResults = errorResults;
	}

	public CommandRegistration getCommandRegistration() {
		return commandRegistration;
	}

	public List<OptionResult> getOptionResults() {
		return optionResults;
	}

	public List<ErrorResult> getErrorResults() {
		return errorResults;
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

	public static class ErrorResult {
		private ParserMessage message;
		private int position;
		private Object[] inserts;
		public ErrorResult(ParserMessage message, int position, Object... inserts) {
			this.message = message;
			this.position = position;
			this.inserts = inserts;
		}
		public ParserMessage getParserMessage() {
			return message;
		}
		public String getMessage() {
			return message.formatMessage(position, inserts);
		}
	}
}
