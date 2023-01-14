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
					CommandInfo info = new CommandInfo(registration, parent);
					rootCommands.computeIfAbsent(commands[i], command -> info);
					parent = info;
				}
				else {
					CommandInfo info = new CommandInfo(registration, parent);
					parent.children.add(info);
					parent = info;
				}
			}
		});
	}

	static class CommandInfo {
		CommandRegistration registration;
		CommandInfo parent;
		List<CommandInfo> children = new ArrayList<>();

		CommandInfo(CommandRegistration registration, CommandInfo parent) {
			this.registration = registration;
			this.parent = parent;
		}

		Map<String, Token> getValidTokens() {
			Map<String, Token> tokens = new HashMap<>();
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
