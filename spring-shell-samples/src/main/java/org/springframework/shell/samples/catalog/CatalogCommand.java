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
package org.springframework.shell.samples.catalog;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.component.TerminalUI;
import org.springframework.shell.component.view.AppView;
import org.springframework.shell.component.view.BoxView;
import org.springframework.shell.component.view.GridView;
import org.springframework.shell.component.view.SelectorListView;
import org.springframework.shell.component.view.StatusBarView;
import org.springframework.shell.component.view.StatusBarView.StatusItem;
import org.springframework.shell.samples.catalog.scenario.Scenario;
import org.springframework.shell.standard.AbstractShellComponent;

/**
 * Main command access point to view showcase catalog.
 *
 * @author Janne Valkealahti
 */
@Command
public class CatalogCommand extends AbstractShellComponent {

	private final Logger log = LoggerFactory.getLogger(CatalogCommand.class);

	@Autowired
	private List<Scenario> scenarios;

	@Command(command = "catalog")
	public void catalog(
	) {
		log.info("Using scenarios {}", scenarios);
		TerminalUI component = new TerminalUI(getTerminal());
		AppView app = new AppView();

		GridView grid = new GridView();
		grid.setRowSize(0, 1);
		grid.setColumnSize(30, 0);

		SelectorListView categories = new SelectorListView();
		// BoxView categories = new BoxView();
		categories.setTitle("Categories");
		categories.setShowBorder(true);

		BoxView scenarios = new BoxView();
		scenarios.setTitle("Scenarios");
		scenarios.setShowBorder(true);

		StatusBarView statusBar = statusBar();

		grid.addItem(categories, 0, 0, 1, 1, 0, 0);
		grid.addItem(scenarios, 0, 1, 1, 1, 0, 0);
		grid.addItem(statusBar, 1, 0, 1, 2, 0, 0);

		app.setMain(grid);

		component.setRoot(app, true);
		component.run();
	}

	private StatusBarView statusBar() {
		StatusBarView statusBar = new StatusBarView();
		StatusItem item1 = new StatusBarView.StatusItem("item1");
		StatusItem item2 = new StatusBarView.StatusItem("item2");
		statusBar.setItems(Arrays.asList(item1, item2));
		return statusBar;
	}

}
