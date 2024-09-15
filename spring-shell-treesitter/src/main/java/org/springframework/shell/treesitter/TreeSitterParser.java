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
package org.springframework.shell.treesitter;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import org.springframework.shell.treesitter.ts.TreeSitter;
import org.springframework.util.Assert;

/**
 *
 * @author Janne Valkealahti
 */
public class TreeSitterParser implements AutoCloseable {

	private MemorySegment parser;
	private final Arena arena;

	public TreeSitterParser(TreeSitterLanguage language) {
		Assert.notNull(language, "language must be");
		arena = Arena.ofShared();
		parser = TreeSitter.ts_parser_new().reinterpret(arena, TreeSitter::ts_parser_delete);
		// TODO: should handle boolean returned from setting language
		TreeSitter.ts_parser_set_language(parser, language.init());
	}

	@Override
	public void close() {
		arena.close();
	}

	public TreeSitterTree parse(String code) {
		return parse(code.getBytes());
	}

	public TreeSitterTree parse(byte[] code) {
		try (Arena a = Arena.ofConfined()) {
			MemorySegment sourceCode = a.allocateFrom(ValueLayout.JAVA_BYTE, code);
			int length = (int) sourceCode.byteSize() - 1;
			MemorySegment tree = TreeSitter.ts_parser_parse_string(parser, MemorySegment.NULL, sourceCode, length);
			return new TreeSitterTree(tree, code);
		}
	}
}
