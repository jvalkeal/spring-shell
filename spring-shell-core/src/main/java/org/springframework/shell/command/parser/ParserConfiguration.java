/*
 * Copyright 2023 the original author or authors.
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
package org.springframework.shell.command.parser;

/**
 * Class containing parser configuration options. These are mostly what default
 * implementations of {@link Parser}, {@link Lexer} and {@link Ast} supports.
 * Custom implementations of those may choose what options to support.
 *
 * @author Janne Valkealahti
 */
public class ParserConfiguration {

	private boolean enableDirectives;

	public boolean isEnableDirectives() {
		return enableDirectives;
	}

	public ParserConfiguration setEnableDirectives(boolean enableDirectives) {
		this.enableDirectives = enableDirectives;
		return this;
	}
}