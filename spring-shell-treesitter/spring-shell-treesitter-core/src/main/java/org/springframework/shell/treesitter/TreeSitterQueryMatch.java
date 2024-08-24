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

import java.util.List;

public class TreeSitterQueryMatch {

	private int id;
	private int patternIndex;
	private int captureCount;
	private int captureIndex;
	private List<TreeSitterQueryCapture> captures;
	private List<String> names;

	public TreeSitterQueryMatch(int id, int patternIndex, int captureIndex, int captureCount, List<TreeSitterQueryCapture> captures, List<String> names) {
		this.id = id;
		this.patternIndex = patternIndex;
		this.captureIndex = captureIndex;
		this.captureCount = captureCount;
		this.captures = captures;
		this.names = names;
	}

	public int getPatternIndex() {
		return patternIndex;
	}

	public int getCaptureIndex() {
		return captureIndex;
	}

	public List<TreeSitterQueryCapture> getCaptures() {
		return captures;
	}

	public List<String> getNames() {
		return names;
	}

}
