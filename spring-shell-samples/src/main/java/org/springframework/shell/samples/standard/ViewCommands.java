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
package org.springframework.shell.samples.standard;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.component.xxx.BoxView;
import org.springframework.shell.component.xxx.GridView;
import org.springframework.shell.component.xxx.ViewComponent;
import org.springframework.shell.standard.AbstractShellComponent;

@Command(command = "view")
public class ViewCommands extends AbstractShellComponent {

	@Autowired
	private TaskScheduler scheduler;

	@Command(command = "box")
	public void box() {
		ViewComponent component = new ViewComponent(getTerminal());
		BoxView box = new BoxView();
		box.setTitle("Title");
		box.setShowBorder(true);
		component.setRoot(box, true);
		component.run();
	}

	@Command(command = "gridx")
	public void gridx() {
		ViewComponent component = new ViewComponent(getTerminal());

		BoxView menu = new BoxView();
		// menu.setTitle("Menu");
		// menu.setShowBorder(true);

		// menu.setDrawFunction((display, rect) -> {
		// 	display.print("Hi", rect.x(), rect.y(), 2);
		// 	return rect;
		// });


		GridView grid = new GridView();
		grid.setTitle("Grid");
		// grid.setShowBorder(true);
		grid.borders = true;

		grid.setRowSize(0);
		grid.setColumnSize(0);
		grid.setShowBorder(true);
		grid.addItem(menu, 0, 0, 1, 1, 0, 0);
		component.setRoot(grid, true);
		component.run();
	}

	@Command(command = "grid")
	public void grid() {
		ViewComponent component = new ViewComponent(getTerminal());

		BoxView menu = new BoxView();
		// menu.setTitle("Menu");
		// menu.setShowBorder(true);

		BoxView main = new BoxView();
		// main.setTitle("Main content");
		// main.setShowBorder(true);

		BoxView sideBar = new BoxView();
		// sideBar.setTitle("Side Bar");
		// sideBar.setShowBorder(true);

		BoxView header = new BoxView();
		// header.setTitle("Header");
		// header.setShowBorder(true);

		BoxView footer = new BoxView();
		// footer.setTitle("Footer");
		// footer.setShowBorder(true);

		GridView grid = new GridView();
		grid.setRowSize(3, 0, 3);
		grid.setColumnSize(30, 0, 30);
		// grid.setShowBorder(true);
		grid.borders = true;
		grid.addItem(header, 0, 0, 1, 3, 0, 0);
		grid.addItem(footer, 2, 0, 1, 3, 0, 0);

		// grid.addItem(menu, 0, 0, 0, 0, 0, 0);
		// grid.addItem(main, 1, 0, 1, 3, 0, 0);
		// grid.addItem(sideBar, 0, 0, 0, 0, 0, 0);

		grid.addItem(menu, 1, 0, 1, 1, 0, 100);
		grid.addItem(main, 1, 1, 1, 1, 0, 100);
		grid.addItem(sideBar, 1, 2, 1, 1, 0, 100);

		component.setRoot(grid, true);
		component.run();
	}

	@Command(command = { "demo", "clock" })
	void demoClock() {
		AtomicReference<String> data = new AtomicReference<>();
		ViewComponent component = new ViewComponent(getTerminal());
		BoxView box = new BoxView();
		box.setTitle("Title");
		box.setShowBorder(true);
		box.setDrawFunction((display, rect) -> {
			String text = data.get();
			if (text != null) {
				display.print(text, rect.x(), rect.y(), text.length());
			}
			return rect;
		});
		component.setRoot(box, true);

		ScheduledFuture<?> task = scheduler.scheduleAtFixedRate(() -> {
			data.set(new Date().toString());
			component.redraw();
		}, Duration.ofSeconds(1));

		component.run();
		task.cancel(true);
	}
}
