package org.springframework.shell.command.parser;

public class DirectiveNode extends SyntaxNode {

	private final String name;
	private final String value;

	public DirectiveNode(Token token, String name, String value) {
		super(token);
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

}
