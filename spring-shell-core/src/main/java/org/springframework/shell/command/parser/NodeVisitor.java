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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.shell.command.CommandRegistration;
import org.springframework.shell.command.parser.CommandModel.CommandInfo;

public class NodeVisitor {

	private final static Logger log = LoggerFactory.getLogger(NodeVisitor.class);

	private CommandModel commandModel;
	private List<String> resolvedCommmand = new ArrayList<>();

	public NodeVisitor(CommandModel commandModel) {
		this.commandModel = commandModel;
	}

	ParseResult visit(CommandNode node) {
		log.debug("visit {}", node);
		resolvedCommmand.add(node.getCommand());
		visitChildren(node);

		CommandInfo info = commandModel.resolve(resolvedCommmand);
		CommandRegistration registration = info.registration;
		return new ParseResult(registration);
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

	private void visitCommandNode(CommandNode node) {
		log.debug("visitCommandNode {}", node);
		resolvedCommmand.add(node.getCommand());
	}

	private void visitOptionNode(OptionNode node) {
		log.debug("visitOptionNode {}", node);
		List<SyntaxNode> children = node.getChildren();
	}

	private void visitCommandArgumentNode(CommandArgumentNode node) {
		log.debug("visitCommandArgumentNode {}", node);
	}

	private void visitOptionArgumentNode(OptionArgumentNode node) {
		log.debug("visitCommandArgumentNode {}", node);
	}

	private void visitChildren(NonterminalSyntaxNode node) {
		log.debug("visitChildren {}", node);
		for (SyntaxNode syntaxNode : node.getChildren()) {
			visitInternal(syntaxNode);
		}
	}
}
