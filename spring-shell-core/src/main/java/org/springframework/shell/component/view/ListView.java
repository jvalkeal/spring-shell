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
package org.springframework.shell.component.view;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.shell.component.view.control.ListCell;
import org.springframework.shell.component.view.event.KeyEvent;
import org.springframework.shell.component.view.event.KeyHandler;
import org.springframework.shell.component.view.geom.Rectangle;
import org.springframework.shell.component.view.message.ShellMessageBuilder;
import org.springframework.shell.component.view.screen.Color;
import org.springframework.shell.component.view.screen.Screen;
import org.springframework.shell.component.view.screen.Screen.Writer;

/**
 * {@link ListView} shows {@link ListItem items} vertically.
 *
 * @author Janne Valkealahti
 */
public class ListView<T> extends BoxView {

	private final static Logger log = LoggerFactory.getLogger(ListView.class);

	private final List<T> items = new ArrayList<>();
	private int selected = -1;

	private final List<ListCell<T>> cells = new ArrayList<>();
	private Function<ListView<T>, ListCell<T>> factory = listView -> new ListCell<>();

	@Override
	protected void drawInternal(Screen screen) {
		Rectangle rect = getInnerRect();
		// Writer writer = screen.writerBuilder().build();
		int y = rect.y();

		int i = 0;
		for (ListCell<T> c : cells) {
			c.setRect(rect.x(), y++, rect.width(), rect.height());
			if (i == selected) {
				c.updateSelected(true);
				c.setBackgroundColor(Color.BLUE4);
			}
			else {
				c.updateSelected(false);
				c.setBackgroundColor(-1);
			}
			c.updateSelected(i == selected);
			c.draw(screen);
			i++;
		}
		// for (T i : items) {
		// 	writer.text(i.toString(), rect.x(), y++);
		// }
		super.drawInternal(screen);
	}

	public void setItems(List<T> items) {
		this.items.clear();
		this.items.addAll(items);
		this.cells.clear();
		for (T i : items) {
			ListCell<T> c = factory.apply(this);
			cells.add(c);
			c.updateItem(i);
		}
	}

	@Override
	public KeyHandler getKeyHandler() {
		return args -> {
			KeyEvent event = args.event();

			if (event.key() == null) {
				String data = event.data();
			}
			else {
				switch (event.key()) {
					// case ENTER:
					// 	log.debug("XXX ENTER");
					// 	break;
					case UP:
						// log.debug("XXX UP");
						up();
						break;
					case DOWN:
						// log.debug("XXX DOWN");
						down();
						break;
					default:
						break;
				}
			}
			return KeyHandler.resultOf(event, null);
		};
	}

	private void up() {
		updateIndex(-1);
		dispatch(ShellMessageBuilder.ofView(this, new ListViewArgs<>(selectedItem(), this)));
	}

	private void down() {
		updateIndex(1);
		dispatch(ShellMessageBuilder.ofView(this, new ListViewArgs<>(selectedItem(), this)));
	}

	private T selectedItem() {
		T selectedItem = null;
		if (selected > 0 && selected < items.size()) {
			selectedItem = items.get(selected);
		}
		return selectedItem;
	}

	private void updateIndex(int step) {
		int size = items.size();
		if (step > 0) {
			if (selected + step < size) {
				selected += step;
			}
		}
		else if (step < 0) {
			if (selected - step > 0) {
				selected += step;
			}
		}
	}

	public static class ListViewArgsx<T> implements ViewAction {

		T selected() {
			return null;
		}

		@Override
		public View view() {
			return null;
		}

	}

	public record ListViewArgs<T>(T selected, View view) implements ViewAction {
	}


}
