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

import org.springframework.shell.treesitter.ts.TSPoint;
import org.springframework.shell.treesitter.ts.TreeSitter;

public class TreeSitterNode {

	private MemorySegment treeSegment;

	public TreeSitterNode(MemorySegment treeSegment) {
		this.treeSegment = treeSegment;
	}

	public MemorySegment getTreeSegment() {
		return treeSegment;
	}

    public TreeSitterPoint getStartPoint() {
		Arena offHeap = Arena.ofConfined();
		MemorySegment nodeStartPoint = TreeSitter.ts_node_start_point(offHeap, treeSegment);
		int startX = TSPoint.column(nodeStartPoint);
		int startY = TSPoint.row(nodeStartPoint);
		return new TreeSitterPoint(startY, startX);
    }

    public TreeSitterPoint getEndPoint() {
		Arena offHeap = Arena.ofConfined();
		MemorySegment nodeEndPoint = TreeSitter.ts_node_end_point(offHeap, treeSegment);
		int endX = TSPoint.column(nodeEndPoint);
		int endY = TSPoint.row(nodeEndPoint);
		return new TreeSitterPoint(endY, endX);
    }

    public int getStartByte() {
        return TreeSitter.ts_node_start_byte(treeSegment);
    }

    public int getEndByte() {
        return TreeSitter.ts_node_end_byte(treeSegment);
    }

}
