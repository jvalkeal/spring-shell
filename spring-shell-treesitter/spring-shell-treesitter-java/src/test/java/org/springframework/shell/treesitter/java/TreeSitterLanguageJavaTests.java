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
package org.springframework.shell.treesitter.java;

import java.lang.foreign.MemorySegment;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.shell.treesitter.TreeSitterNativeLoader;
import org.springframework.shell.treesitter.TreeSitterParser;
import org.springframework.shell.treesitter.TreeSitterQuery;
import org.springframework.shell.treesitter.TreeSitterQueryMatch;
import org.springframework.shell.treesitter.TreeSitterTree;
import org.springframework.shell.treesitter.java.ts.TreeSitterJava;

import static org.assertj.core.api.Assertions.assertThat;

public class TreeSitterLanguageJavaTests {

	private final static String CODE1 = """
			[1, null]
			""";

	private final static String CODE2 = """
			package org.example;
			class HelloWorld {
    			public static void main(String[] args) {
        			System.out.println("Hello, World!");
    			}
			}
			""";

	@Test
	void languageLoads() {
		TreeSitterNativeLoader.initializeLanguage("java");
		MemorySegment segment = TreeSitterJava.tree_sitter_java();
		assertThat(segment).isNotNull();
	}

	@Test
	void canParse() {
		TreeSitterNativeLoader.initialize();
		TreeSitterNativeLoader.initializeLanguage("java");
		TreeSitterLanguageJava java = new TreeSitterLanguageJava();
		TreeSitterQuery query = new TreeSitterQuery(java, TreeSitterLanguageJava.QUERY_HIGHLIGHT);
		TreeSitterParser parser = new TreeSitterParser();
		parser.setLanguage(java);
		TreeSitterTree tree = parser.parse(CODE2);
		List<TreeSitterQueryMatch> matches = query.findMatches(tree.getRootNode());
		assertThat(matches).isNotNull();
	}
}
