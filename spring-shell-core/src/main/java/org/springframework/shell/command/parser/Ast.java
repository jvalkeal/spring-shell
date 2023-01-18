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

/**
 * Interface to generate abstract syntax tree from tokens. Generic language
 * parser usually contains lexing and parsing where this {@code Ast} represents
 * the latter parsing side.
 *
 * @author Janne Valkealahti
 */
public interface Ast {

	/**
	 * Generate ast result from a tokens. {@link AstResult} contains info about
	 * token to ast tree generation.
	 *
	 * @param tokens the tokens
	 * @return a result containing further syntax info
	 */
	AstResult generate(List<Token> tokens);

	/**
	 * Interface representing result from tokens to ast tree generation.
	 */
	public interface AstResult {

		/**
		 * Gets resolved {@link CommandNode}.
		 *
		 * @return resolver command node
		 */
		CommandNode getCommandNode();
	}

	/**
	 * Default implementation of an {@link Ast}.
	 */
	public static class DefaultAst implements Ast {

		@Override
		public AstResult generate(List<Token> tokens) {
			List<CommandNode> commandNodes = new ArrayList<>();
			CommandNode commandNode = null;
			OptionNode optionNode = null;
			for (Token token : tokens) {
				switch (token.getType()) {
					case DIRECTIVE:
						break;
					case COMMAND:
						commandNode = new CommandNode(token, token.getValue());
						commandNodes.add(commandNode);
						break;
					case OPTION:
						optionNode = new OptionNode(token, token.getValue());
						commandNode.addChildNode(optionNode);
						break;
					case ARGUMENT:
						if (optionNode != null) {
							OptionArgumentNode optionArgumentNode = new OptionArgumentNode(token, optionNode, token.getValue());
							optionNode.addChildNode(optionArgumentNode);
						}
						else {
							CommandArgumentNode commandArgumentNode = new CommandArgumentNode(token, commandNode);
							commandNode.addChildNode(commandArgumentNode);
						}
						break;
					case DOUBLEDASH:
						optionNode = null;
						break;
					default:
						break;
				}
			}
			return new DefaultAstResult(commandNodes.get(commandNodes.size() - 1));
		}

	}

	static class DefaultAstResult implements AstResult {

		private final CommandNode commandNode;

		DefaultAstResult(CommandNode commandNode) {
			this.commandNode = commandNode;
		}

		@Override
		public CommandNode getCommandNode() {
			return commandNode;
		}
	}

}
