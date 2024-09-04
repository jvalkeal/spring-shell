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
import java.lang.foreign.ValueLayout.OfInt;
import java.util.ArrayList;
import java.util.List;

import org.springframework.shell.treesitter.ts.TSNode;
import org.springframework.shell.treesitter.ts.TSQueryCapture;
import org.springframework.shell.treesitter.ts.TSQueryMatch;
import org.springframework.shell.treesitter.ts.TreeSitter;
import org.springframework.util.Assert;

/**
 *
 * @author Janne Valkealahti
 */
public class TreeSitterQuery implements AutoCloseable {

	private TreeSitterLanguage language;
	private String source;
	private final Arena arena;

	public TreeSitterQuery(TreeSitterLanguage language, String source) {
		Assert.notNull(language, "language must be");
		Assert.notNull(source, "source must be");
		arena = Arena.ofShared();
		this.language = language;
		this.source = source;
	}

	@Override
	public void close() {
		arena.close();
	}

	public List<TreeSitterQueryMatch> findMatches(TreeSitterNode node) {
		MemorySegment cursor = TreeSitter.ts_query_cursor_new().reinterpret(arena, TreeSitter::ts_query_cursor_delete);
		MemorySegment languageSegment = language.init();
		MemorySegment sourceSegment = arena.allocateFrom(source);
		int sourceLen = source.length();
		MemorySegment error_offset = arena.allocateFrom(OfInt.JAVA_INT, 0);
		MemorySegment error_type = arena.allocateFrom(OfInt.JAVA_INT, 0);
		MemorySegment querySegment = TreeSitter
				.ts_query_new(languageSegment, sourceSegment, sourceLen, error_offset, error_type)
				.reinterpret(arena, TreeSitter::ts_query_delete);
		TreeSitter.ts_query_cursor_exec(cursor, querySegment, node.getNode());

		MemorySegment queryMatch = TSQueryMatch.allocate(arena);

		int captureCount = TreeSitter.ts_query_capture_count(querySegment);
		List<String> captureNames = new ArrayList<>(captureCount);

		for (int i = 0; i < captureCount; i++) {
			MemorySegment length = arena.allocate(ValueLayout.JAVA_INT);
			MemorySegment name = TreeSitter.ts_query_capture_name_for_id(querySegment, i, length);
			captureNames.add(name.getString(0));
		}

		List<TreeSitterQueryMatch> matches = new ArrayList<>();

		while (TreeSitter.ts_query_cursor_next_match(cursor, queryMatch)) {
			short patternIndex = TSQueryMatch.pattern_index(queryMatch);
			short count = TSQueryMatch.capture_count(queryMatch);
			int id = TSQueryMatch.id(queryMatch);


			MemorySegment captures = TSQueryMatch.captures(queryMatch);

			List<TreeSitterQueryCapture> queryCaptures = new ArrayList<>();

			List<String> names = new ArrayList<>();
			for (short i = 0; i < count; ++i) {
				MemorySegment capture = TSQueryCapture.asSlice(captures, i);
				String name = captureNames.get(TSQueryCapture.index(capture));
				MemorySegment captureNode = TSNode.allocate(arena).copyFrom(TSQueryCapture.node(capture));
				TreeSitterNode treeSitterNode = TreeSitterNode.of(captureNode);
				TreeSitterQueryCapture treeSitterQueryCapture = new TreeSitterQueryCapture(treeSitterNode, i);
				queryCaptures.add(treeSitterQueryCapture);
				names.add(name);
			}

			int index = TSQueryCapture.index(captures);
			TreeSitterQueryMatch treeSitterQueryMatch = new TreeSitterQueryMatch(id, patternIndex, index, captureCount, queryCaptures, names);
			matches.add(treeSitterQueryMatch);
		}

		return matches;
	}
}
