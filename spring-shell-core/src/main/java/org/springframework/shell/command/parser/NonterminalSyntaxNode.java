package org.springframework.shell.command.parser;

import java.util.ArrayList;
import java.util.List;

public abstract class NonterminalSyntaxNode extends SyntaxNode {

	private final List<SyntaxNode> children = new ArrayList<>();

	public NonterminalSyntaxNode(Token token) {
		super(token);
	}

	public List<SyntaxNode> getChildren() {
		return children;
	}

	void addChildNode(SyntaxNode node) {
		this.children.add(node);
	}

}
