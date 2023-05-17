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
package org.springframework.shell.samples.view;

import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.shell.component.TerminalUI;
import org.springframework.shell.component.view.BoxView;
import org.springframework.shell.component.view.GridView;
import org.springframework.shell.component.view.InputView;
import org.springframework.shell.standard.AbstractShellComponent;

/**
 * Commands showing some default features of a build-in views.
 *
 * @author Janne Valkealahti
 */
@Command(command = "view")
public class ViewCommands extends AbstractShellComponent {

	@Command(command = "box")
	public void box() {
		TerminalUI component = new TerminalUI(getTerminal());
		BoxView box = new BoxView();
		box.setTitle("Title");
		box.setShowBorder(true);
		component.setRoot(box, true);
		component.run();
	}

	@Command(command = "grid")
	public void grid() {
		TerminalUI component = new TerminalUI(getTerminal());

		BoxView menu = new BoxView();
		// menu.setTitle("Menu");
		// menu.setShowBorder(true);

		BoxView main = new BoxView();

		BoxView sideBar = new BoxView();

		BoxView header = new BoxView();

		BoxView footer = new BoxView();

		GridView grid = new GridView();
		grid.setRowSize(3, 0, 3);
		grid.setColumnSize(30, 0, 30);
		// grid.setShowBorder(true);
		grid.setShowBorders(true);
		grid.addItem(header, 0, 0, 1, 3, 0, 0);
		grid.addItem(footer, 2, 0, 1, 3, 0, 0);

		grid.addItem(menu, 0, 0, 0, 0, 0, 0);
		grid.addItem(main, 1, 0, 1, 3, 0, 0);
		grid.addItem(sideBar, 0, 0, 0, 0, 0, 0);

		grid.addItem(menu, 1, 0, 1, 1, 0, 100);
		grid.addItem(main, 1, 1, 1, 1, 0, 100);
		grid.addItem(sideBar, 1, 2, 1, 1, 0, 100);

		component.setRoot(grid, true);
		component.run();
	}

	@Command(command = "input")
	public String input(
		@Option(defaultValue = "true") boolean fullScreen
	) {
		TerminalUI component = new TerminalUI(getTerminal());
		InputView input = new InputView();
		input.setTitle("Input");
		input.setShowBorder(true);
		component.setRoot(input, fullScreen);
		component.run();
		return input.getInputText();
	}

	@Command(command = "test")
	public void test() {
	}

}
