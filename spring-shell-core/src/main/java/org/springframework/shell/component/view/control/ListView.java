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
package org.springframework.shell.component.view.control;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.jline.terminal.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.shell.component.view.control.cell.ListCell;
import org.springframework.shell.component.view.event.KeyEvent;
import org.springframework.shell.component.view.event.KeyHandler;
import org.springframework.shell.component.view.event.MouseHandler;
import org.springframework.shell.component.view.geom.Rectangle;
import org.springframework.shell.component.view.message.ShellMessageBuilder;
import org.springframework.shell.component.view.screen.Color;
import org.springframework.shell.component.view.screen.Screen;

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
			c.setRect(rect.x(), y++, rect.width(), 1);
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
		super.drawInternal(screen);
	}

	public void setCellFactory(Function<ListView<T>, ListCell<T>> factory) {
		this.factory = factory;
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
		// if (!this.items.isEmpty()) {
		// 	this.selected = 0;
		// }
	}

	@Override
	public KeyHandler getKeyHandler() {
		log.trace("getKeyHandler()");
		return args -> {
			KeyEvent event = args.event();
			boolean consumed = true;
			if (event.key() != null) {
				switch (event.key()) {
					case UP -> {
						up();
					}
					case DOWN -> {
						down();
					}
					case ENTER -> {
						enter();
					}
					default -> {
						consumed = false;
					}
				}
			}
			return KeyHandler.resultOf(event, consumed, null);
		};
	}

	@Override
	public MouseHandler getMouseHandler() {
		log.trace("getMouseHandler() {}");
		MouseHandler handler = args -> {
			View view = null;
			MouseEvent event = args.event();
			int x = event.getX();
			int y = event.getY();
			if (getRect().contains(x, y)) {
				if (event.getModifiers().isEmpty() && event.getType() == MouseEvent.Type.Wheel) {
					if (event.getButton() == MouseEvent.Button.WheelDown) {
						view = this;
						down();
					}
					else if (event.getButton() == MouseEvent.Button.WheelUp) {
						view = this;
						up();
					}
				}
				else if (event.getModifiers().isEmpty() && event.getType() == MouseEvent.Type.Released
						&& event.getButton() == MouseEvent.Button.Button1) {
					view = this;
				}

			}
			return MouseHandler.resultOf(args.event(), view != null, view, this);
		};
		return super.getMouseHandler().fromIfNotConsumed(handler);
	}

	private void up() {
		updateIndex(-1);
		dispatch(ShellMessageBuilder.ofView(this, new ListViewAction<>(selectedItem(), "LineUp", this)));
	}

	private void down() {
		updateIndex(1);
		dispatch(ShellMessageBuilder.ofView(this, new ListViewAction<>(selectedItem(), "LineDown", this)));
	}

	private void enter() {
		log.info("XXX enter");
		dispatch(ShellMessageBuilder.ofView(this, new ListViewAction<>(selectedItem(), "OpenSelectedItem", this)));
	}

	public void setSelected(int selected) {
		if (this.selected != selected) {
			this.selected = selected;
			dispatch(ShellMessageBuilder.ofView(this, new ListViewAction<>(selectedItem(), "SelectedChanged", this)));
		}
	}

	private T selectedItem() {
		T selectedItem = null;
		if (selected >= 0 && selected < items.size()) {
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

	public record ListViewAction<T>(T item, String action, View view) implements ViewAction {
	}

}
