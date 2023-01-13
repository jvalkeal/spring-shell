package org.springframework.shell.command.parser;

public class CommandNode extends NonterminalSyntaxNode {

	private final String command;

	public CommandNode(Token token, String command) {
		super(token);
		this.command = command;
	}

	public String getCommand() {
		return command;
	}

}
