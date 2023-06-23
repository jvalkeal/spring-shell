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
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import org.jline.terminal.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.shell.component.view.control.MenuView.MenuItem;
import org.springframework.shell.component.view.event.KeyHandler;
import org.springframework.shell.component.view.event.MouseHandler;
import org.springframework.shell.component.view.geom.Rectangle;
import org.springframework.shell.component.view.screen.Color;
import org.springframework.shell.component.view.screen.Screen;
import org.springframework.shell.component.view.screen.Screen.Writer;

/**
 * {@link MenuBarView} shows {@link MenuBarItem items} horizontally and is
 * typically used in layouts which builds complete terminal UI's.
 *
 * @author Janne Valkealahti
 */
public class MenuBarView extends BoxView {

	private final Logger log = LoggerFactory.getLogger(MenuBarView.class);
	private final List<MenuBarItem> items = new ArrayList<>();

	MenuView menuView;
	MenuBarItem itemAt;

	public MenuBarView(MenuBarItem[] items) {
		setItems(Arrays.asList(items));
	}

	@Override
	protected void drawInternal(Screen screen) {
		Rectangle rect = getInnerRect();
		log.debug("Drawing menu bar to {}", rect);
		Writer writer = screen.writerBuilder().color(Color.RED).build();
		int x = rect.x();
		ListIterator<MenuBarItem> iter = items.listIterator();
		while (iter.hasNext()) {
			MenuBarItem item = iter.next();
			String text = String.format(" %s%s", item.getTitle(), iter.hasNext() ? " " : "");
			writer.text(text, x, rect.y());
			x += text.length();
		}
		if (menuView != null) {
			menuView.draw(screen);
		}
		super.drawInternal(screen);
	}

	@Override
	public KeyHandler getKeyHandler() {
		if (menuView != null) {
			return menuView.getKeyHandler();
		}
		return super.getKeyHandler();
	}

	@Override
	public MouseHandler getMouseHandler() {
		log.trace("getMouseHandler()");
		return args -> {
			View view = null;
			MouseEvent event = args.event();
			if (event.getModifiers().isEmpty() && event.getType() == MouseEvent.Type.Released
					&& event.getButton() == MouseEvent.Button.Button1) {
				int x = event.getX();
				int y = event.getY();
				if (getInnerRect().contains(x, y)) {
					view = this;
				}

				MenuBarItem itemAt = itemAt(x, y);
				log.info("XXX itemAt {} {} {}", x, y, itemAt);
				if (itemAt != null) {
					if (this.itemAt == itemAt) {
						this.menuView = null;
						this.itemAt = null;
					}
					else {
						MenuView menuView = new MenuView(itemAt.getItems());
						menuView.setShowBorder(true);
						menuView.setBackgroundColor(Color.AQUAMARINE4);
						menuView.setLayer(1);
						Rectangle rect = getInnerRect();
						menuView.setRect(rect.x(), rect.y()+1, 15, 10);
						this.menuView = menuView;
						this.itemAt = itemAt;
					}
				}
				else if (menuView != null) {
					menuView.getMouseHandler().handle(args);
				}
			}
			return MouseHandler.resultOf(args.event(), true, view, null);
		};
	}

	private MenuBarItem itemAt(int x, int y) {
		Rectangle rect = getRect();
		if (!rect.contains(x, y)) {
			return null;
		}
		int ix = 0;
		for (MenuBarItem item : items) {
			if (x < ix + item.getTitle().length()) {
				return item;
			}
			ix += item.getTitle().length();
		}
		return null;
	}

	/**
	 * Sets items.
	 *
	 * @param items status items
	 */
	public void setItems(List<MenuBarItem> items) {
		this.items.clear();
		this.items.addAll(items);
	}

	/**
	 * {@link MenuBarItem} represents an item in a {@link StatusBarView}.
	 */
	public static class MenuBarItem {

		private String title;
		private List<MenuItem> items;

		public MenuBarItem(String title) {
			this(title, null);
		}

		public MenuBarItem(String title, MenuItem[] items) {
			this.title = title;
			this.items = Arrays.asList(items);
		}

		public String getTitle() {
			return title;
		}

		public List<MenuItem> getItems() {
			return items;
		}
	}

}
