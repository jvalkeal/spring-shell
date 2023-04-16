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
import org.springframework.shell.component.xxx.ViewComponent;
import org.springframework.shell.standard.AbstractShellComponent;

@Command(command = "view")
public class ViewCommands extends AbstractShellComponent {

	@Autowired
	private TaskScheduler scheduler;

	@Command(command = "box")
	public void box() {
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
