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
		assertThat(result.getCommandNode()).isNotNull();
		assertThat(result.getCommandNode().getCommand()).isEqualTo("root1");
		assertThat(result.getCommandNode().getToken()).isNotNull();
	}

	@Test
	void createsOptionNodeNoOptionArg() {
		register(ROOT3);
		AstResult result = ast(tokenize("root3", "--arg1"));

		assertThat(result).isNotNull();
		assertThat(result.getCommandNode()).isNotNull();
		assertThat(result.getCommandNode().getCommand()).isEqualTo("root3");
		assertThat(result.getCommandNode().getToken()).isNotNull();
		assertThat(result.getCommandNode().getChildren()).hasSize(1);
		assertThat(result.getCommandNode().getChildren())
			.filteredOn(sn -> sn instanceof OptionNode)
			.extracting(sn -> {
				return ((OptionNode)sn).getName();
			})
			.containsExactly("--arg1");
	}

	@Test
	void createsOptionNodeWithOptionArg() {
		register(ROOT3);
		AstResult result = ast(tokenize("root3", "--arg1", "value1"));

		assertThat(result).isNotNull();
		assertThat(result.getCommandNode()).isNotNull();
		assertThat(result.getCommandNode().getCommand()).isEqualTo("root3");
		assertThat(result.getCommandNode().getToken()).isNotNull();
		assertThat(result.getCommandNode().getChildren()).hasSize(1);
		assertThat(result.getCommandNode().getChildren())
			.filteredOn(sn -> sn instanceof OptionNode)
			.extracting(sn -> {
				return ((OptionNode)sn).getName();
			})
			.containsExactly("--arg1");
		SyntaxNode x1 = result.getCommandNode().getChildren().get(0);
		SyntaxNode x2 = ((OptionNode)x1).getChildren().get(0);
		OptionArgumentNode x3 = (OptionArgumentNode)x2;
		assertThat(x3.getValue()).isEqualTo("value1");
	}
}
