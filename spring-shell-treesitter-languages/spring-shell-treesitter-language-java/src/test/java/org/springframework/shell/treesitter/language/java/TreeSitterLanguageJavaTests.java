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
package org.springframework.shell.treesitter.language.java;

import java.lang.foreign.MemorySegment;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import org.springframework.shell.treesitter.TreeSitterNativeLoader;
import org.springframework.shell.treesitter.language.java.ts.TreeSitterJava;

import static org.assertj.core.api.Assertions.assertThat;

public class TreeSitterLanguageJavaTests {

	@Test
	@Tag("treesitter")
	void languageLoads() {
		TreeSitterNativeLoader.initializeLanguage("java");
		MemorySegment segment = TreeSitterJava.tree_sitter_java();
		assertThat(segment).isNotNull();
	}

}