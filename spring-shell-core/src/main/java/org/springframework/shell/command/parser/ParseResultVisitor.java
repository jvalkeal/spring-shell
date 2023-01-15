package org.springframework.shell.command.parser;

import java.util.List;

public class ParseResultVisitor {


	void visit(CommandNode node) {

		visitChildren(node);
	}

	private void visitInternal(SyntaxNode node) {

		if (node instanceof CommandNode n) {
			visitCommandNode(n);
			visitChildren(n);
		}
		else if (node instanceof OptionNode n) {
			visitOptionNode(n);
			visitChildren(n);
		}
		else if (node instanceof CommandArgumentNode n) {
			visitCommandArgumentNode(n);
		}
		else if (node instanceof OptionArgumentNode n) {
			visitOptionArgumentNode(n);
		}

	}

	private void visitCommandNode(CommandNode node) {
	}

	private void visitOptionNode(OptionNode node) {
	}

	private void visitCommandArgumentNode(CommandArgumentNode node) {
	}

	private void visitOptionArgumentNode(OptionArgumentNode node) {
	}

	private void visitChildren(NonterminalSyntaxNode parentNode) {
		for (SyntaxNode syntaxNode : parentNode.getChildren()) {
			visitInternal(syntaxNode);
		}
	}
}

// public void doAction(Object o) {
//     return switch (o) {
//         case A a -> doA(a);
//         case B b -> doB(b);
//         case C c -> doC(c);
//         default -> log.warn("Unrecognized type of {}", o);
//     };
// }
