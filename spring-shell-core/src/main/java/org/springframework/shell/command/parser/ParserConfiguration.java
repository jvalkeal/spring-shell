package org.springframework.shell.command.parser;

public class ParserConfiguration {

	private boolean enableDirectives;

	public boolean isEnableDirectives() {
		return enableDirectives;
	}

	public void setEnableDirectives(boolean enableDirectives) {
		this.enableDirectives = enableDirectives;
	}
}
