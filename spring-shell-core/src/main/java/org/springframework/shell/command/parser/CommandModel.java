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

			CommandInfo info = null;
			for (int i = 0; i < commands.length; i++) {
				CommandRegistration parent = info != null ? e.getValue() : null;
				info = rootCommands.computeIfAbsent(commands[i],
						command -> new CommandInfo(e.getValue(), parent));
			}
		});
	}

	static class CommandInfo {
		CommandRegistration registration;
		CommandRegistration parent;
		List<CommandRegistration> children = new ArrayList<>();

		CommandInfo(CommandRegistration registration, CommandRegistration parent) {
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
			e.getValue().registration.getAliases().forEach(alias -> {
				String[] commands = alias.getCommand().split(" ");
				tokens.put(commands[0], new Token(commands[0], TokenType.COMMAND));
			});
		});
		return tokens;
	}
}
