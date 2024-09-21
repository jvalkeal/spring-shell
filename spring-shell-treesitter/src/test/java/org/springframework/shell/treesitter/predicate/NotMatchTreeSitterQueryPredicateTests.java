/*
 * Copyright 2024 the original author or authors.
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
package org.springframework.shell.treesitter.predicate;

import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import org.springframework.shell.treesitter.TreeSitterNode;
import org.springframework.shell.treesitter.TreeSitterQueryCapture;
import org.springframework.shell.treesitter.TreeSitterQueryMatch;
import org.springframework.shell.treesitter.TreeSitterTree;
import org.springframework.shell.treesitter.predicate.TreeSitterQueryPredicate.TreeSitterQueryPredicateContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class NotMatchTreeSitterQueryPredicateTests {

	@Test
	void test1() {
		TreeSitterTree tree = mock(TreeSitterTree.class);
		TreeSitterNode node = mock(TreeSitterNode.class);
		TreeSitterQueryMatch match = mock(TreeSitterQueryMatch.class);
		TreeSitterQueryCapture tsqc = mock(TreeSitterQueryCapture.class);
		doReturn("test").when(tsqc).getName();
		doReturn(List.of(tsqc)).when(match).getCaptures();
		doReturn(node).when(tsqc).getNode();
		doReturn(tree).when(node).getTree();
		doReturn(0).when(node).getStartByte();
		doReturn(11).when(node).getEndByte();
		doReturn("fakecontent".getBytes()).when(tree).getContent();

		// Pattern pattern = Pattern.compile("fakecontent");
		Pattern pattern = Pattern.compile("fakenotcontent");
		NotMatchTreeSitterQueryPredicate predicate = new NotMatchTreeSitterQueryPredicate("test", pattern);
		TreeSitterQueryPredicateContext context = new TreeSitterQueryPredicate.TreeSitterQueryPredicateContext(match);
		boolean test = predicate.test(context);
		assertThat(test).isTrue();
	}

}
