package org.springframework.shell.command.parser;

public abstract class SyntaxNode {

	private final Token token;

	public SyntaxNode(Token token) {
		this.token = token;
	}

	public Token getToken() {
		return token;
	}
}
