/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.shell.command.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base abstract {@link NodeVisitor} which visits all nodes allowing user to
 * implement callback methods.
 *
 * @author Janne Valkealahti
 */
public abstract class AbstractNodeVisitor implements NodeVisitor {

	private final static Logger log = LoggerFactory.getLogger(AbstractNodeVisitor.class);

	public final ParseResult visit(CommandNode node) {
		log.debug("visit {}", node);
		onRootCommandNode(node);
		visitChildren(node);
		return buildResult();
	}

	/**
	 * Called after all nodes has been visited to build results.
	 *
	 * @return the results from this visit operation
	 */
	protected abstract ParseResult buildResult();

	/**
	 * Callback when root command node is visited.
	 *
	 * @param node the root command node
	 */
	protected abstract void onRootCommandNode(CommandNode node);

	/**
	 * Callback when command node is visited.
	 *
	 * @param node the command node
	 */
	protected abstract void onCommandNode(CommandNode node);

	/**
	 * Callback when option node is visited.
	 *
	 * @param node the option node
	 */
	protected abstract void onOptionNode(OptionNode node);

	/**
	 * Callback when command argument node is visited.
	 *
	 * @param node the command argument node
	 */
	protected abstract void onCommandArgumentNode(CommandArgumentNode node);

	/**
	 * Callback when option argument node is visited.
	 *
	 * @param node the option argument node
	 */
	protected abstract void onOptionArgumentNode(OptionArgumentNode node);

	private void visitCommandNode(CommandNode node) {
		log.debug("visitCommandNode {}", node);
		onCommandNode(node);
	}

	private void visitOptionNode(OptionNode node) {
		log.debug("visitOptionNode {}", node);
		onOptionNode(node);
	}

	private void visitCommandArgumentNode(CommandArgumentNode node) {
		log.debug("visitCommandArgumentNode {}", node);
		onCommandArgumentNode(node);
	}

	private void visitOptionArgumentNode(OptionArgumentNode node) {
		log.debug("visitCommandArgumentNode {}", node);
		onOptionArgumentNode(node);
	}

	private void visitChildren(NonterminalSyntaxNode node) {
		log.debug("visitChildren {}", node);
		for (SyntaxNode syntaxNode : node.getChildren()) {
			visitInternal(syntaxNode);
		}
	}

	private void visitInternal(SyntaxNode node) {
		log.debug("visitInternal {}", node);
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
}
