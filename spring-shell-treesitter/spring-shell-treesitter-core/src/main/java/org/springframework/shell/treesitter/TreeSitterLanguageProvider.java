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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public interface TreeSitterLanguageProvider {

	boolean supports(String languageName);

	static TreeSitterLanguageProvider load(String name) throws IOException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if (cl == null) {
			cl = ClassLoader.getSystemClassLoader();
		}
		InputStream is = cl.getResourceAsStream("META-INF/services/org/springframework/shell/treesitter/language/provider/" + name);
		if (is != null) {
			Properties props = new Properties();
			try {
				props.load(is);
				String className = props.getProperty("class");
				if (className == null) {
					throw new IOException("No class defined in language provider file " + name);
				}
				Class<?> clazz = cl.loadClass(className);
				return (TreeSitterLanguageProvider) clazz.getConstructor().newInstance();
			} catch (Exception e) {
				throw new IOException("Unable to load language provider " + name + ": " + e.getMessage(), e);
			}
		} else {
			throw new IOException("Unable to find language provider " + name);
		}
	}

}
