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
package org.springframework.shell.treesitter.json;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import org.springframework.shell.treesitter.TreeSitterLanguage;
import org.springframework.shell.treesitter.json.ts.TreeSitterJson;

public class TreeSitterLanguageJson extends TreeSitterLanguage {

	public final static String QUERY_HIGHLIGHT = """
			(pair
			  key: (_) @string.special.key)

			(string) @string

			(number) @number

			[
			  (null)
			  (true)
			  (false)
			] @constant.builtin

			(escape_sequence) @escape

			(comment) @comment
			""";

	@Override
	public MemorySegment getLanguageSegment() {
		MemorySegment json = TreeSitterJson.tree_sitter_json();
		return json;
	}



}
