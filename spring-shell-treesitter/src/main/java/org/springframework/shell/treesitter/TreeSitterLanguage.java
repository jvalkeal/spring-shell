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

import java.lang.foreign.MemorySegment;

/**
 * Low level {@code treesitter} interface initialising its language and having
 * info about query.
 *
 * @author Janne Valkealahti
 */
public interface TreeSitterLanguage {

	/**
	 * Initialise a {@code treesitter} language returning its {@link MemorySegment}.
	 * Memory is expected to get freed by {@code treesitter parser}.
	 *
	 * @return a memory segment for initialised language
	 */
	MemorySegment init();

	/**
	 * Get a {@code treesitter} {@code highlight query}.
	 *
	 * @return treesitter highlight query
	 */
	String highlightQuery();

}
