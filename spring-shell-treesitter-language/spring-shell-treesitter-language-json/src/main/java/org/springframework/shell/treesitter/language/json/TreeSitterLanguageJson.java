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
package org.springframework.shell.treesitter.language.json;

import java.lang.foreign.MemorySegment;
import java.util.List;

import org.springframework.shell.treesitter.TreeSitterLanguage;
import org.springframework.shell.treesitter.language.json.ts.TreeSitterJson;

public class TreeSitterLanguageJson extends TreeSitterLanguage {

	@Override
	public boolean supports(String languageName) {
		return "json".equals(languageName);
	}

	@Override
	public List<String> supportedLanguages() {
		return List.of("json");
	}

	@Override
	public TreeSitterLanguageJson language() {
		return this;
	}

	@Override
	public String highlightQuery() {
		return resourceAsString(resourceLoader.getResource("classpath:org/springframework/shell/treesitter/queries/json/highlights.scm"));
	}

	@Override
	public MemorySegment init() {
		MemorySegment json = TreeSitterJson.tree_sitter_json();
		return json;
	}

}
