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

	@Override
	public final ParseResult visit(CommandNode node) {
		log.debug("visit {}", node);
		log.debug("onEnterRootCommandNode {}", node);
		onEnterRootCommandNode(node);
		visitChildren(node);
		log.debug("onExitRootCommandNode {}", node);
		onExitRootCommandNode(node);
		return buildResult();
	}

	/**
	 * Called after all nodes has been visited to build results.
	 *
	 * @return the results from this visit operation
	 */
	protected abstract ParseResult buildResult();

	protected abstract void onEnterRootCommandNode(CommandNode node);

	protected abstract void onExitRootCommandNode(CommandNode node);

	protected abstract void onEnterCommandNode(CommandNode node);

	protected abstract void onExitCommandNode(CommandNode node);

	protected abstract void onEnterOptionNode(OptionNode node);

	protected abstract void onExitOptionNode(OptionNode node);

	protected abstract void onEnterCommandArgumentNode(CommandArgumentNode node);

	protected abstract void onExitCommandArgumentNode(CommandArgumentNode node);

	protected abstract void onEnterOptionArgumentNode(OptionArgumentNode node);

	protected abstract void onExitOptionArgumentNode(OptionArgumentNode node);

	private void visitChildren(NonterminalSyntaxNode node) {
		log.debug("visitChildren {}", node);
		for (SyntaxNode syntaxNode : node.getChildren()) {
			visitInternal(syntaxNode);
		}
	}

	private void enterCommandNode(CommandNode node) {
		log.debug("enterCommandNode {}", node);
		onEnterCommandNode(node);
	}

	private void exitCommandNode(CommandNode node) {
		log.debug("exitCommandNode {}", node);
		onExitCommandNode(node);
	}

	private void enterOptionNode(OptionNode node) {
		log.debug("enterOptionNode {}", node);
		onEnterOptionNode(node);
	}

	private void exitOptionNode(OptionNode node) {
		log.debug("exitOptionNode {}", node);
		onExitOptionNode(node);
	}

	private void enterCommandArgumentNode(CommandArgumentNode node) {
		log.debug("enterCommandArgumentNode {}", node);
		onEnterCommandArgumentNode(node);
	}

	private void exitCommandArgumentNode(CommandArgumentNode node) {
		log.debug("exitCommandArgumentNode {}", node);
		onExitCommandArgumentNode(node);
	}

	private void enterOptionArgumentNode(OptionArgumentNode node) {
		log.debug("enterOptionArgumentNode {}", node);
		onEnterOptionArgumentNode(node);
	}

	private void exitOptionArgumentNode(OptionArgumentNode node) {
		log.debug("exitOptionArgumentNode {}", node);
		onExitOptionArgumentNode(node);
	}

	private void visitInternal(SyntaxNode node) {
		log.debug("visitInternal {}", node);
		if (node instanceof CommandNode n) {
			enterCommandNode(n);
			visitChildren(n);
			exitCommandNode(n);
		}
		else if (node instanceof OptionNode n) {
			enterOptionNode(n);
			visitChildren(n);
			exitOptionNode(n);
		}
		else if (node instanceof CommandArgumentNode n) {
			enterCommandArgumentNode(n);
			exitCommandArgumentNode(n);
		}
		else if (node instanceof OptionArgumentNode n) {
			enterOptionArgumentNode(n);
			exitOptionArgumentNode(n);
		}
	}
}
