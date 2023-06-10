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
package org.springframework.shell.samples.catalog.scenario.box;

import org.springframework.shell.component.view.BoxView;
import org.springframework.shell.component.view.View;
import org.springframework.shell.samples.catalog.scenario.AbstractScenario;
import org.springframework.shell.samples.catalog.scenario.ScenarioComponent;

import static org.springframework.shell.samples.catalog.scenario.Scenario.CATEGORY_BOX1;

@ScenarioComponent(name = "box1title", description = "box1 desc", category = { CATEGORY_BOX1 })
public class SimpleBoxView1Scenario extends AbstractScenario {

	@Override
	public View build() {
		BoxView box = new BoxView();
		box.setEventLoop(getEventloop());
		box.setTitle("box1title");
		box.setShowBorder(true);
		return box;
	}
}
