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

/**
 * Interface providing {@link TreeSitterLanguage} and what languages it
 * supports. These are separated so that we don't need to initialise actual
 * backing {@code tree-sitter} libraries order to know what languages it
 * supports.
 *
 * @author Janne Valkealahti
 */
public interface TreeSitterLanguageProvider {

	/**
	 * Checks if this provider supports a {@code language}.
	 *
	 * @param languageName the language name
	 * @return {@code true} if language is supported
	 */
	boolean supports(String languageName);

	/**
	 * Gets a list of supported languges.
	 *
	 * @return a list of supported languages
	 */
	List<String> supportedLanguages();

	/**
	 * Get a {@link TreeSitterLanguage} this provider handles.
	 *
	 * @return a treesitter language
	 */
	TreeSitterLanguage language();

}
