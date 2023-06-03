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
package org.springframework.shell.samples.catalog.scenario;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Base implementation of a {@link Scenario} helping to avoid some bloatware.
 *
 * @author Janne Valkealahti
 */
public class AbstractScenario implements Scenario {

	private final List<String> categories = new ArrayList<>();
	private final String title;

	public AbstractScenario(String title, String... category) {
		this(title, Arrays.asList(category));
	}

	public AbstractScenario(String title, List<String> categories) {
		this.categories.addAll(categories);
		this.title = title;
	}

	@Override
	public List<String> getCategories() {
		return categories;
	}

	@Override
	public String getTitle() {
		return title;
	}

}
