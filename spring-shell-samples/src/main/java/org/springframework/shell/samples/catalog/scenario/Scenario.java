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

/**
 * Scenario is an item which participates in a catalog showcase command.
 *
 * @author Janne Valkealahti
 */
public interface Scenario {

	/**
	 * Gets a categories this scenario should be attached.
	 *
	 * @return categories scenario should be attached
	 */
	List<String> getCategories();

}
