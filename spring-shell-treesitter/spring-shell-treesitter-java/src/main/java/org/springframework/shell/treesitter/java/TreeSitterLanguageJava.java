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

import org.springframework.shell.treesitter.TreeSitterLanguage;
import org.springframework.shell.treesitter.java.ts.TreeSitterJava;

public class TreeSitterLanguageJava extends TreeSitterLanguage<TreeSitterLanguageJava> {

	@Override
	public boolean supports(String languageName) {
		return "java".equals(languageName);
	}

	@Override
	public List<String> supportedLanguages() {
		return List.of("java");
	}

	@Override
	public TreeSitterLanguageJava getLanguage() {
		return this;
	}

	@Override
	public String highlightQuery() {
		return QUERY_HIGHLIGHT;
	}

	public final static String QUERY_HIGHLIGHT = """
			; Variables

			(identifier) @variable

			; Methods

			(method_declaration
			  name: (identifier) @function.method)
			(method_invocation
			  name: (identifier) @function.method)
			(super) @function.builtin

			; Annotations

			(annotation
			  name: (identifier) @attribute)
			(marker_annotation
			  name: (identifier) @attribute)

			"@" @operator

			; Types

			(type_identifier) @type

			(interface_declaration
			  name: (identifier) @type)
			(class_declaration
			  name: (identifier) @type)
			(enum_declaration
			  name: (identifier) @type)

			((field_access
			  object: (identifier) @type)
			 (#match? @type "^[A-Z]"))
			((scoped_identifier
			  scope: (identifier) @type)
			 (#match? @type "^[A-Z]"))
			((method_invocation
			  object: (identifier) @type)
			 (#match? @type "^[A-Z]"))
			((method_reference
			  . (identifier) @type)
			 (#match? @type "^[A-Z]"))

			(constructor_declaration
			  name: (identifier) @type)

			[
			  (boolean_type)
			  (integral_type)
			  (floating_point_type)
			  (floating_point_type)
			  (void_type)
			] @type.builtin

			; Constants

			((identifier) @constant
			 (#match? @constant "^_*[A-Z][A-Z\\d_]+$"))

			; Builtins

			(this) @variable.builtin

			; Literals

			[
			  (hex_integer_literal)
			  (decimal_integer_literal)
			  (octal_integer_literal)
			  (decimal_floating_point_literal)
			  (hex_floating_point_literal)
			] @number

			[
			  (character_literal)
			  (string_literal)
			] @string
			(escape_sequence) @string.escape

			[
			  (true)
			  (false)
			  (null_literal)
			] @constant.builtin

			[
			  (line_comment)
			  (block_comment)
			] @comment

			; Keywords

			[
			  "abstract"
			  "assert"
			  "break"
			  "case"
			  "catch"
			  "class"
			  "continue"
			  "default"
			  "do"
			  "else"
			  "enum"
			  "exports"
			  "extends"
			  "final"
			  "finally"
			  "for"
			  "if"
			  "implements"
			  "import"
			  "instanceof"
			  "interface"
			  "module"
			  "native"
			  "new"
			  "non-sealed"
			  "open"
			  "opens"
			  "package"
			  "private"
			  "protected"
			  "provides"
			  "public"
			  "requires"
			  "record"
			  "return"
			  "sealed"
			  "static"
			  "strictfp"
			  "switch"
			  "synchronized"
			  "throw"
			  "throws"
			  "to"
			  "transient"
			  "transitive"
			  "try"
			  "uses"
			  "volatile"
			  "while"
			  "with"
			] @keyword
			""";

	@Override
	public MemorySegment getLanguageSegment() {
		MemorySegment java = TreeSitterJava.tree_sitter_java();
		return java;
	}



}
