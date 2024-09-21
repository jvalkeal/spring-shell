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

/**
 *
 * @author Janne Valkealahti
 */
public class TreeSitterQueryCapture {

	private TreeSitterNode node;
	private int index;
	private String name;

	public TreeSitterQueryCapture(TreeSitterNode node, int index, String name) {
		this.node = node;
		this.index = index;
		this.name = name;
	}

	public TreeSitterNode getNode() {
		return node;
	}

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

}
