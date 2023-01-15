package org.springframework.shell.command.parser;

public class CommandArgumentNode extends SyntaxNode {

	private final CommandNode parent;

	public CommandArgumentNode(Token token, CommandNode parent) {
		super(token);
		this.parent = parent;
	}

	public CommandNode getParentCommandNode() {
		return parent;
	}
}
