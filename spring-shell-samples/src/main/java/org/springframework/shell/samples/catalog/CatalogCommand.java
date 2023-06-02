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

import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.component.TerminalUI;
import org.springframework.shell.component.view.AppView;
import org.springframework.shell.component.view.BoxView;
import org.springframework.shell.component.view.GridView;
import org.springframework.shell.component.view.screen.Color;
import org.springframework.shell.standard.AbstractShellComponent;

@Command
public class CatalogCommand extends AbstractShellComponent {

	@Command(command = "catalog")
	public void catalog(
	) {
		TerminalUI component = new TerminalUI(getTerminal());
		AppView app = new AppView();

		GridView grid = new GridView();
		grid.setShowBorders(true);
		grid.setRowSize(0);
		grid.setColumnSize(30, 0, 30);

		BoxView g1 = new BoxView();
		BoxView g2 = new BoxView();
		BoxView g3 = new BoxView();

		grid.addItem(g1, 0, 0, 1, 1, 0, 0);
		grid.addItem(g2, 0, 1, 1, 1, 0, 0);
		grid.addItem(g3, 0, 2, 1, 1, 0, 0);

		app.setMain(grid);

		BoxView modal = new BoxView();
		modal.setBackgroundColor(Color.KHAKI4);
		modal.setTitle("Modal");
		modal.setShowBorder(true);

		app.setModal(modal);

		component.setRoot(app, true);
		component.run();
	}

}
