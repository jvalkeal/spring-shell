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
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

/**
 *
 * @author Janne Valkealahti
 */
public class TreeSitterLanguages {

	private TreeSitterServices<TreeSitterLanguageProvider> services;
	private ResourceLoader resourceLoader;

	public TreeSitterLanguages(ConfigurableListableBeanFactory beanFactory, ResourceLoader resourceLoader) {
		this(TreeSitterServices.factoriesAndBeans(beanFactory));
		this.resourceLoader = resourceLoader;
	}

	TreeSitterLanguages(TreeSitterServices.Loader loader) {
		services = loader.load(TreeSitterLanguageProvider.class);
	}

	public TreeSitterLanguageProvider getLanguageProvider(String language) {
		TreeSitterLanguageProvider provider = services.asList().stream()
			.filter(l -> l.supports(language)).findFirst()
			.orElseThrow(() -> new RuntimeException(String.format("Language %s not supported", language)));
		if (provider instanceof ResourceLoaderAware rla) {
			rla.setResourceLoader(resourceLoader);
		}
		return provider;
	}

	public List<TreeSitterLanguageProvider> getLanguageProviders() {
		return services.asList();
	}

	public List<String> getSupportedLanguages() {
		return getLanguageProviders().stream()
			.flatMap(provider -> provider.supportedLanguages().stream())
			.toList();
	}

	public List<TreeSitterQueryMatch> languageMatch(String languageId, byte[] bytes) {
		TreeSitterNativeLoader.initialize();
		TreeSitterNativeLoader.initializeLanguage(languageId);

		TreeSitterLanguageProvider provider = getLanguageProvider(languageId);
		TreeSitterLanguage language = provider.language();

		TreeSitterTree tree = null;
		try (TreeSitterParser parser = new TreeSitterParser(language)) {
			tree = parser.parse(new String(bytes));
		}

		List<TreeSitterQueryMatch> matches = null;
		try (TreeSitterQuery query = new TreeSitterQuery(language, language.highlightQuery());) {
			matches = query.findMatches(tree.getRootNode());
		}

		return matches;
	}


}
