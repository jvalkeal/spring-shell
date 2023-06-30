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
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.ListIterator;

import org.jline.terminal.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;

import org.springframework.shell.component.view.control.MenuView.MenuItem;
import org.springframework.shell.component.view.event.KeyEvent;
import org.springframework.shell.component.view.event.KeyEvent.KeyType;
import org.springframework.shell.component.view.event.KeyHandler;
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
	private MenuView currentMenuView;
	private int activeItemIndex = -1;

	/**
	 * Construct menubar view with menubar items.
	 *
	 * @param items the menubar items
	 */
	public MenuBarView(MenuBarItem[] items) {
		setItems(Arrays.asList(items));
	}

	public static MenuBarView of(MenuBarItem... items) {
		return new MenuBarView(items);
	}

	/**
	 * Sets a selected index. If given index is not within bounds of size of items,
	 * selection is set to {@code -1} effectively un-selecting active item.
	 *
	 * @param index the new index
	 */
	public void setSelected(int index) {
		if (index >= items.size() || index < 0) {
			activeItemIndex = -1;
		}
		else {
			activeItemIndex = index;
		}
	}

	@Override
	protected void initInternal() {
		registerRunnableCommand(ViewCommand.LINE_UP, () -> up());
		registerRunnableCommand(ViewCommand.LINE_DOWN, () -> down());
		registerRunnableCommand(ViewCommand.LEFT, () -> left());
		registerRunnableCommand(ViewCommand.RIGHT, () -> right());

		registerKeyBinding(KeyType.UP, ViewCommand.LINE_UP);
		registerKeyBinding(KeyType.DOWN, ViewCommand.LINE_DOWN);
		registerKeyBinding(KeyType.LEFT, ViewCommand.LEFT);
		registerKeyBinding(KeyType.RIGHT, ViewCommand.RIGHT);

		registerMouseBindingConsumerCommand(ViewCommand.SELECT, event -> select(event));

		registerMouseBinding(MouseEvent.Type.Released, MouseEvent.Button.Button1,
				EnumSet.noneOf(MouseEvent.Modifier.class), ViewCommand.SELECT);
	}

	private void up() {
		if (activeItemIndex > 0) {
			setSelected(activeItemIndex - 1);
		}
	}

	private void down() {
		if (activeItemIndex + 1 < items.size()) {
			setSelected(activeItemIndex + 1);
		}
	}

	private void left() {
	}

	private void right() {
	}

	private int indexAtPosition(int x, int y) {
		Rectangle rect = getRect();
		if (!rect.contains(x, y)) {
			return -1;
		}
		int i = 0;
		int p = 1;
		for (MenuBarItem item : items) {
			p += item.getTitle().length() + 1;
			if (x < p) {
				return i;
			}
			i++;
		}
		return -1;
	}

	private int positionAtIndex(int index) {
		int i = 0;
		int x = 1;
		for (MenuBarItem item : items) {
			if (i == index) {
				return x;
			}
			x += item.getTitle().length() + 1;
			i++;
		}
		return x;
	}

	private void select(MouseEvent event) {
		int x = event.getX();
		int y = event.getY();
		int i = indexAtPosition(x, y);
		if (i > -1) {
			if (i == activeItemIndex) {
				setSelected(-1);
			}
			else {
				setSelected(i);
			}
		}
		checkMenuView();
	}

	private void checkMenuView() {
		if (activeItemIndex < 0) {
			closeCurrentMenuView();
		}
		else {
			MenuBarItem item = items.get(activeItemIndex);
			currentMenuView = buildMenuView(item);
		}
	}

	private void closeCurrentMenuView() {
		if (currentMenuView != null) {
			currentMenuView.destroy();
		}
		currentMenuView = null;
	}

	private MenuView buildMenuView(MenuBarItem item) {
		MenuView menuView = new MenuView(item.getItems());
		menuView.setEventLoop(getEventLoop());
		menuView.setShowBorder(true);
		menuView.setBackgroundColor(Color.AQUAMARINE4);
		menuView.setLayer(1);
		Integer max = item.getItems().stream()
			.map(i -> i.getTitle().length())
			.max(Comparator.naturalOrder())
			.orElse(1);
		Rectangle rect = getInnerRect();
		int x = positionAtIndex(activeItemIndex);
		menuView.setRect(rect.x() + x, rect.y() + 1, max + 2, items.size() + 2);

		Disposable d = getEventLoop().viewActions(MenuView.MenuViewAction.class, menuView)
			.doOnNext(a -> {
				log.info("XXX action2 {}", a);
				if (ViewCommand.OPEN_SELECTED_ITEM == a.action()) {
					closeCurrentMenuView();
				}
			})
			.subscribe();
		menuView.onDestroy(d);
		return menuView;
	}

	@Override
	protected void drawInternal(Screen screen) {
		Rectangle rect = getInnerRect();
		log.debug("Drawing menu bar to {}", rect);
		Writer writer1 = screen.writerBuilder().build();
		Writer writer2 = screen.writerBuilder().color(Color.RED).build();
		int x = rect.x();
		ListIterator<MenuBarItem> iter = items.listIterator();
		while (iter.hasNext()) {
			MenuBarItem item = iter.next();
			int index = iter.previousIndex();
			Writer writer = activeItemIndex == index ? writer2 : writer1;
			String text = String.format(" %s%s", item.getTitle(), iter.hasNext() ? " " : "");
			writer.text(text, x, rect.y());
			x += text.length();
		}
		if (currentMenuView != null) {
			currentMenuView.draw(screen);
		}
		super.drawInternal(screen);
	}

	@Override
	public KeyHandler getKeyHandler() {
		KeyHandler handler = args -> {
			KeyEvent event = args.event();
			KeyHandler h = null;
			if (currentMenuView != null) {
				h = currentMenuView.getKeyHandler();
			}
			if (h == null) {
				h = super.getKeyHandler();
			}
			if (h != null) {
				return h.handle(args);
			}
			return KeyHandler.resultOf(event, false, null);
		};
		return handler;
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
	 * {@link MenuBarItem} represents an item in a {@link MenuBarView}.
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

		public static MenuBarItem of(String title, MenuItem... items) {
			return new MenuBarItem(title, items);
		}

		public String getTitle() {
			return title;
		}

		public List<MenuItem> getItems() {
			return items;
		}
	}

}
