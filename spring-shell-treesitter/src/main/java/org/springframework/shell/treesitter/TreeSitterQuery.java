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

/**
 *
 * @author Janne Valkealahti
 */
public class TreeSitterQuery {

	private AbstractTreeSitterLanguage language;
	private String source;

	public TreeSitterQuery(AbstractTreeSitterLanguage language, String source) {
		this.language = language;
		this.source = source;
	}

	public List<TreeSitterQueryMatch> findMatches(TreeSitterNode node) {
		// try (Arena arena = Arena.ofConfined()) {
		// }

		Arena offHeap = Arena.ofConfined();
		MemorySegment cursor = TreeSitter.ts_query_cursor_new();
		// cursor.reinterpret(offHeap, segment -> {
		// 	TreeSitter.ts_query_cursor_delete(segment);
		// });
		MemorySegment languageSegment = language.init();
		MemorySegment sourceSegment = offHeap.allocateFrom(source);
		int sourceLen = source.length();
		MemorySegment error_offset = offHeap.allocateFrom(OfInt.JAVA_INT, 0);
		MemorySegment error_type = offHeap.allocateFrom(OfInt.JAVA_INT, 0);
		MemorySegment querySegment = TreeSitter.ts_query_new(languageSegment, sourceSegment, sourceLen, error_offset, error_type);
		TreeSitter.ts_query_cursor_exec(cursor, querySegment, node.getTreeSegment());

		MemorySegment queryMatch = TSQueryMatch.allocate(offHeap);

		int captureCount = TreeSitter.ts_query_capture_count(querySegment);
		List<String> captureNames = new ArrayList<>(captureCount);

		for (int i = 0; i < captureCount; i++) {
			MemorySegment length = offHeap.allocate(ValueLayout.JAVA_INT);
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
				MemorySegment captureNode = TSNode.allocate(offHeap).copyFrom(TSQueryCapture.node(capture));
				TreeSitterNode treeSitterNode = new TreeSitterNode(captureNode);
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
