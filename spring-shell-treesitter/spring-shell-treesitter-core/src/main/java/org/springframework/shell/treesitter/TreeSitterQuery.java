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
import org.springframework.shell.treesitter.ts.TSPoint;
import org.springframework.shell.treesitter.ts.TSQueryCapture;
import org.springframework.shell.treesitter.ts.TSQueryMatch;
import org.springframework.shell.treesitter.ts.TreeSitter;

public class TreeSitterQuery {

	private TreeSitterLanguage language;
	private String source;

	public TreeSitterQuery(TreeSitterLanguage language, String source) {
		this.language = language;
		this.source = source;
	}

	public List<TreeSitterQueryMatch> findMatches(TreeSitterNode node) {
		Arena offHeap = Arena.ofConfined();
		MemorySegment cursor = TreeSitter.ts_query_cursor_new();
		MemorySegment languageSegment = language.getLanguageSegment();
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
			// MemorySegment captureNode = TSQueryCapture.node(captures);

			// MemorySegment nodeStartPoint = TreeSitter.ts_node_start_point(offHeap, captureNode);
			// MemorySegment nodeEndPoint = TreeSitter.ts_node_end_point(offHeap, captureNode);
			// int startX = TSPoint.column(nodeStartPoint);
			// int startY = TSPoint.row(nodeStartPoint);
			// int endX = TSPoint.column(nodeEndPoint);
			// int endY = TSPoint.row(nodeEndPoint);

			// int startByte = TreeSitter.ts_node_start_byte(captureNode);
			// int endByte = TreeSitter.ts_node_end_byte(captureNode);

			// MemorySegment str = TreeSitter.ts_node_string(captureNode);
			// String nodeStr = str.getString(0);
			// String nodeStr = String.format("(%s, %s)", startByte, endByte);
			// String keyStr = names.getLast();
			// String format = String.format("pattern: %s, capture: %s - %s, start: (%s, %s), end: (%s, %s), text: `%s`",
			// 		patternIndex, index, keyStr, startY, startX, endY, endX, nodeStr);
			// pattern:  0, capture: 0 - string.special.key, start: (1, 4), end: (1, 15), text: `"stringkey"`
			// System.out.println(format);

			TreeSitterQueryMatch treeSitterQueryMatch = new TreeSitterQueryMatch(id, patternIndex, index, captureCount, queryCaptures);
			matches.add(treeSitterQueryMatch);
		}

		return matches;
	}
}
