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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.shell.command.CommandRegistration;

public class CommandModel {

	Map<String, CommandInfo> rootCommands = new HashMap<>();

	CommandModel(Map<String, CommandRegistration> registrations) {
		registrations.entrySet().forEach(e -> {
			String[] commands = e.getKey().split(" ");
			CommandInfo parent = null;
			for (int i = 0; i < commands.length; i++) {
				CommandRegistration registration = i + 1 == commands.length ? e.getValue() : null;
				if (parent == null) {
					CommandInfo info = new CommandInfo(commands[i], registration, parent);
					rootCommands.computeIfAbsent(commands[i], command -> info);
					parent = info;
				}
				else {
					CommandInfo info = new CommandInfo(commands[i], registration, parent);
					parent.children.add(info);
					parent = info;
				}
			}
		});
	}

	static class CommandInfo {
		String command;
		CommandRegistration registration;
		CommandInfo parent;
		List<CommandInfo> children = new ArrayList<>();

		CommandInfo(String command, CommandRegistration registration, CommandInfo parent) {
			this.registration = registration;
			this.parent = parent;
			this.command = command;
		}

		Map<String, Token> getValidTokens() {
			Map<String, Token> tokens = new HashMap<>();
			children.forEach(commandInfo -> {

			});
			registration.getOptions().forEach(commandOption -> {
				for (String longName : commandOption.getLongNames()) {
					tokens.put(longName, new Token(longName, TokenType.OPTION));
				}
			});
			return tokens;
		}
	}

	CommandInfo getRootCommand(String command) {
		return rootCommands.get(command);
	}

	Map<String, Token> getValidRootTokens() {
		Map<String, Token> tokens = new HashMap<>();
		rootCommands.entrySet().forEach(e -> {
			tokens.put(e.getKey(), new Token(e.getKey(), TokenType.COMMAND));
			if (e.getValue().registration != null) {
				e.getValue().registration.getAliases().forEach(alias -> {
					String[] commands = alias.getCommand().split(" ");
					tokens.put(commands[0], new Token(commands[0], TokenType.COMMAND));
				});
			}
		});
		return tokens;
	}
}
