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

/**
 *
 * @author Janne Valkealahti
 */
public final class TreeSitterNode {

	private final MemorySegment node;
	private final TreeSitterPoint startPoint;
	private final TreeSitterPoint endPoint;
	private final int startByte;
	private final int endByte;
	private TreeSitterTree tree;

	private TreeSitterNode(MemorySegment node, TreeSitterTree tree, TreeSitterPoint startPoint, TreeSitterPoint endPoint,
			int startByte, int endByte) {
		this.node = node;
		this.tree = tree;
		this.startPoint = startPoint;
		this.endPoint = endPoint;
		this.startByte = startByte;
		this.endByte = endByte;
	}

	public TreeSitterTree getTree() {
		return tree;
	}

	protected static TreeSitterNode of(MemorySegment node, TreeSitterTree tree) {
		try (Arena arena = Arena.ofConfined()) {
			MemorySegment nodeStartPoint = TreeSitter.ts_node_start_point(arena, node);
			MemorySegment nodeEndPoint = TreeSitter.ts_node_end_point(arena, node);
			TreeSitterPoint startPoint = new TreeSitterPoint(TSPoint.row(nodeStartPoint), TSPoint.column(nodeStartPoint));
			TreeSitterPoint endPoint = new TreeSitterPoint(TSPoint.row(nodeEndPoint), TSPoint.column(nodeEndPoint));
			int startByte = TreeSitter.ts_node_start_byte(node);
			int endByte = TreeSitter.ts_node_end_byte(node);
			return new TreeSitterNode(node, tree, startPoint, endPoint, startByte, endByte);
		}
	}

    public TreeSitterPoint getStartPoint() {
		return startPoint;
    }

    public TreeSitterPoint getEndPoint() {
		return endPoint;
    }

    public int getStartByte() {
		return startByte;
    }

    public int getEndByte() {
		return endByte;
    }

	protected MemorySegment getNode() {
		return node;
	}

}
