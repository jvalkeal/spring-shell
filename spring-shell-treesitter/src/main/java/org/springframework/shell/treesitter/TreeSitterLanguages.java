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

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

public class TreeSitterLanguages {

	private TreeSitterServices<TreeSitterLanguageProvider> services;

	public TreeSitterLanguages(ConfigurableListableBeanFactory beanFactory) {
		this(TreeSitterServices.factoriesAndBeans(beanFactory));
	}

	TreeSitterLanguages(TreeSitterServices.Loader loader) {
		services = loader.load(TreeSitterLanguageProvider.class);
	}

	public TreeSitterLanguageProvider getLanguage(String language) {
		return services.asList().stream()
			.filter(l -> l.supports(language)).findFirst()
			.orElseThrow(() -> new RuntimeException(String.format("Language %s not supported", language)));
	}

	public List<TreeSitterLanguageProvider> getLanguages() {
		return services.asList();
	}

	public List<String> getSupportedLanguages() {
		return getLanguages().stream().flatMap(l -> l.supportedLanguages().stream()).toList();
	}

}
