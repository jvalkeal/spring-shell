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

import org.springframework.shell.treesitter.ts.TreeSitter;

/**
 *
 * @author Janne Valkealahti
 */
public class TreeSitterTree {

	private MemorySegment tree;

	public TreeSitterTree(MemorySegment tree) {
		this.tree = tree;
	}

	public TreeSitterNode getRootNode() {
		Arena arena = Arena.ofShared();
		MemorySegment node = TreeSitter.ts_tree_root_node(arena, tree);
		return TreeSitterNode.of(node);
	}
}
