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

import org.junit.jupiter.api.Test;

import org.springframework.shell.command.parser.Ast.AstResult;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for default implementation in an {@link Ast}.
 *
 * @author Janne Valkealahti
 */
class AstTests extends AbstractParsingTests {

	@Test
	void createsCommandNode() {
		register(ROOT1);
		AstResult result = ast(tokenize("root1"));

		assertThat(result).isNotNull();
		assertThat(result.getNonterminalNodes()).hasSize(1);
		assertThat(result.getNonterminalNodes().get(0)).isInstanceOf(CommandNode.class);
		assertThat(result.getNonterminalNodes().get(0)).satisfies(n -> {
			CommandNode cn = (CommandNode)n;
			assertThat(cn.getCommand()).isEqualTo("root1");
		});
	}

	@Test
	void createsOptionNodeNoOptionArg() {
		register(ROOT3);
		AstResult result = ast(tokenize("root3", "--arg1"));

		assertThat(result).isNotNull();
		assertThat(result.getNonterminalNodes()).hasSize(1);
		assertThat(result.getNonterminalNodes().get(0)).isInstanceOf(CommandNode.class);
		assertThat(result.getNonterminalNodes().get(0)).satisfies(n -> {
			CommandNode cn = (CommandNode)n;
			assertThat(cn.getCommand()).isEqualTo("root3");
			assertThat(cn.getChildren()).hasSize(1);
			assertThat(cn.getChildren())
				.filteredOn(on -> on instanceof OptionNode)
				.extracting(on -> {
					return ((OptionNode)on).getName();
				})
				.containsExactly("--arg1");
		});
	}

	@Test
	void createsOptionNodeWithOptionArg() {
		register(ROOT3);
		AstResult result = ast(tokenize("root3", "--arg1", "value1"));

		assertThat(result).isNotNull();

		assertThat(result).isNotNull();
		assertThat(result.getNonterminalNodes()).hasSize(1);
		assertThat(result.getNonterminalNodes().get(0)).isInstanceOf(CommandNode.class);
		assertThat(result.getNonterminalNodes().get(0)).satisfies(n -> {
			CommandNode cn = (CommandNode)n;
			assertThat(cn.getCommand()).isEqualTo("root3");
			assertThat(cn.getChildren()).hasSize(1);
			assertThat(cn.getChildren())
				.filteredOn(on -> on instanceof OptionNode)
				.extracting(on -> {
					return ((OptionNode)on).getName();
				})
				.containsExactly("--arg1");
			OptionNode on = (OptionNode) cn.getChildren().get(0);
			assertThat(on.getChildren()).hasSize(1);
			OptionArgumentNode oan = (OptionArgumentNode) on.getChildren().get(0);
			assertThat(oan.getValue()).isEqualTo("value1");
		});
	}

	@Test
	void directiveWithCommand() {
		register(ROOT1);
		ParserConfiguration configuration = new ParserConfiguration().setEnableDirectives(true);
		AstResult result = ast(tokenize(lexer(configuration), "[fake]", "root1"));

		assertThat(result.getTerminalNodes()).hasSize(1);
		assertThat(result.getTerminalNodes().get(0)).isInstanceOf(DirectiveNode.class);
		DirectiveNode dn = (DirectiveNode) result.getTerminalNodes().get(0);
		assertThat(dn.getName()).isEqualTo("fake");
		assertThat(dn.getValue()).isNull();
	}

}
