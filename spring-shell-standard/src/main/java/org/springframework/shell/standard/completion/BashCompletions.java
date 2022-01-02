/*
 * Copyright 2022 the original author or authors.
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
package org.springframework.shell.standard.completion;

import java.util.List;

import org.springframework.core.io.ResourceLoader;

public class BashCompletions extends AbstractCompletions {

	public BashCompletions(ResourceLoader resourceLoader) {
		super(resourceLoader);
	}

	public String generate(String rootCommand, List<String> commands) {
		return builder()
				.withDefaultAttribute("name", rootCommand)
				.withDefaultMultiAttribute("commands", commands)
				.appendResourceWithRender("classpath:completion/bash/pre-template.txt")
				.appendResourceWithRender("classpath:completion/bash/root-template.txt")
				.appendResourceWithRender("classpath:completion/bash/post-template.txt")
				.build();
	}

}
