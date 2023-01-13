package org.springframework.shell.command.parser;

public class OptionArgumentNode extends SyntaxNode {

	private final OptionNode parentOptionNode;

	public OptionArgumentNode(Token token, OptionNode parentOptionNode) {
		super(token);
		this.parentOptionNode = parentOptionNode;
	}

	public OptionNode getParentOptionNode() {
		return parentOptionNode;
	}

}
