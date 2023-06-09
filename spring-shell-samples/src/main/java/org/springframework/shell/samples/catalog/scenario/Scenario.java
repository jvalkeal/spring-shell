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

import java.util.List;

import org.springframework.shell.component.view.View;
import org.springframework.shell.component.view.event.EventLoop;

/**
 * {@link Scenario} participates in a catalog showcase.
 *
 * @author Janne Valkealahti
 */
public interface Scenario {

	/**
	 * Gets a categories this scenario should be attached.
	 *
	 * @return categories of a scenario
	 */
	List<String> getCategories();

	/**
	 * Gets a title this scenario should use.
	 *
	 * @return title of a scenario
	 */
	String getTitle();

	/**
	 * Gets a {@link View} to be shown with a scenario.
	 *
	 * @return view of a scenario
	 */
	View getView();

	void configure(EventLoop eventloop);

}
