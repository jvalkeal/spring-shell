package org.springframework.shell.command.parser;

public class Token {

	private final static int IMPLICIT_POSITION = -1;
	private final String value;
	private final TokenType type;
	private final int position;

	public Token(String value, TokenType type) {
		this(value, type, IMPLICIT_POSITION);
	}

	public Token(String value, TokenType type, int position) {
		this.value = value;
		this.type = type;
		this.position = position;
	}

	public String getValue() {
		return value;
	}

	public TokenType getType() {
		return type;
	}

	public int getPosition() {
		return position;
	}

}
