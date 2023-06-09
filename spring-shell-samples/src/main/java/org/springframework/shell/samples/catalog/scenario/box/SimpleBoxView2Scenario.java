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
import org.springframework.stereotype.Component;

@Component
public class SimpleBoxView2Scenario extends AbstractScenario {

	public SimpleBoxView2Scenario() {
		super("box2title", "box2");
	}

	@Override
	public View getView() {
		BoxView box = new BoxView();
		box.setEventLoop(getEventloop());
		box.setTitle("box2title");
		box.setShowBorder(true);
		return box;
	}
}
