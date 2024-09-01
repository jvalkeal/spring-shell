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
package org.springframework.shell.treesitter.language.yaml;

import java.lang.foreign.MemorySegment;
import java.util.List;

import org.springframework.shell.treesitter.TreeSitterLanguage;
import org.springframework.shell.treesitter.language.yaml.ts.TreeSitterYaml;

public class TreeSitterLanguageYaml extends TreeSitterLanguage {

	@Override
	public boolean supports(String languageName) {
		return "yaml".equals(languageName);
	}

	@Override
	public List<String> supportedLanguages() {
		return List.of("yaml");
	}

	@Override
	public TreeSitterLanguageYaml getLanguage() {
		return this;
	}

	@Override
	public String highlightQuery() {
		return resourceAsString(resourceLoader.getResource("classpath:org/springframework/shell/treesitter/queries/yaml/highlights.scm"));
	}

	@Override
	public MemorySegment init() {
		MemorySegment yaml = TreeSitterYaml.tree_sitter_yaml();
		return yaml;
	}

}
