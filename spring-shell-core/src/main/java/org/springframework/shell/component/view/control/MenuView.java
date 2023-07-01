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
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.jline.terminal.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.lang.Nullable;
import org.springframework.shell.component.view.event.KeyEvent.KeyType;
import org.springframework.shell.component.view.geom.Rectangle;
import org.springframework.shell.component.view.message.ShellMessageBuilder;
import org.springframework.shell.component.view.screen.Color;
import org.springframework.shell.component.view.screen.Screen;
import org.springframework.shell.component.view.screen.Screen.Writer;
import org.springframework.shell.component.view.screen.ScreenItem;

/**
 * {@link MenuView} shows {@link MenuView} items vertically and is
 * typically used in layouts which builds complete terminal UI's.
 *
 * @author Janne Valkealahti
 */
public class MenuView extends BoxView {

	private final Logger log = LoggerFactory.getLogger(MenuView.class);
	private final List<MenuItem> items = new ArrayList<>();
	private int activeItemIndex = -1;

	/**
	 * Construct menu view with no initial menu items.
	 */
	public MenuView() {
		this(new MenuItem[0]);
	}

	/**
	 * Construct menu view with menu items.
	 *
	 * @param items the menu items
	 */
	public MenuView(MenuItem[] items) {
		this(items != null ? Arrays.asList(items) : Collections.emptyList());
	}

	/**
	 * Construct menu view with menu items.
	 *
	 * @param items the menu items
	 */
	public MenuView(@Nullable List<MenuItem> items) {
		setItems(items);
	}

	/**
	 * Sets a new menu items. Will always clear existing items and if {@code null}
	 * is passed this effectively keeps items empty.
	 *
	 * @param items the menu items
	 */
	public void setItems(@Nullable List<MenuItem> items) {
		this.items.clear();
		activeItemIndex = -1;
		if (items != null) {
			this.items.addAll(items);
			if (!items.isEmpty()) {
				activeItemIndex = 0;
			}
		}
	}

	/**
	 * Gets a menu items.
	 *
	 * @return the menu items
	 */
	public List<MenuItem> getItems() {
		return items;
	}

	@Override
	protected void drawInternal(Screen screen) {
		Rectangle rect = getInnerRect();
		int y = rect.y();
		Writer writer = screen.writerBuilder().layer(getLayer()).build();
		Writer writer2 = screen.writerBuilder().layer(getLayer()).color(Color.RED).style(ScreenItem.STYLE_ITALIC).build();
		int i = 0;
		for (MenuItem item : items) {
			if (activeItemIndex == i) {
				writer2.text(item.getTitle(), rect.x(), y);
			}
			else {
				writer.text(item.getTitle(), rect.x(), y);
			}
			i++;
			y++;
		}
		super.drawInternal(screen);
	}

	@Override
	protected void initInternal() {
		registerRunnableCommand(ViewCommand.LINE_UP, () -> move(-1));
		registerRunnableCommand(ViewCommand.LINE_DOWN, () -> move(1));
		registerRunnableCommand(ViewCommand.OPEN_SELECTED_ITEM, () -> keySelect());

		registerKeyBinding(KeyType.UP, ViewCommand.LINE_UP);
		registerKeyBinding(KeyType.DOWN, ViewCommand.LINE_DOWN);
		registerKeyBinding(KeyType.ENTER, ViewCommand.OPEN_SELECTED_ITEM);

		registerMouseBindingConsumerCommand(ViewCommand.SELECT, event -> mouseSelect(event));

		registerMouseBinding(MouseEvent.Type.Released, MouseEvent.Button.Button1,
				EnumSet.noneOf(MouseEvent.Modifier.class), ViewCommand.SELECT);

		registerMouseBindingConsumerCommand(ViewCommand.LINE_DOWN, event -> move(1));
		registerMouseBindingConsumerCommand(ViewCommand.LINE_UP, event -> move(-1));
		registerMouseBinding(MouseEvent.Type.Wheel, MouseEvent.Button.WheelDown,
				EnumSet.noneOf(MouseEvent.Modifier.class), ViewCommand.LINE_DOWN);
		registerMouseBinding(MouseEvent.Type.Wheel, MouseEvent.Button.WheelUp,
				EnumSet.noneOf(MouseEvent.Modifier.class), ViewCommand.LINE_UP);

	}

	private void keySelect() {
		dispatch(ShellMessageBuilder.ofView(this, new MenuViewAction(ViewCommand.OPEN_SELECTED_ITEM, this)));
	}

	private void move(int count) {
		log.trace("move({})", count);
		setSelected(activeItemIndex + count);
	}

	private void setSelected(int index) {
		if (index >= items.size()) {
			activeItemIndex = 0;
		}
		else if(index < 0) {
			activeItemIndex = items.size() - 1;
		}
		else {
			if (activeItemIndex != index) {
				activeItemIndex = index;
				MenuItem item = items.get(index);
				dispatch(ShellMessageBuilder.ofView(this, new MenuViewItemAction(item, ViewCommand.SELECTION_CHANGED, this)));
			}
		}
	}

	private void mouseSelect(MouseEvent event) {
		log.trace("select({})", event);
		int x = event.getX();
		int y = event.getY();
		setSelected(indexAtPosition(x, y));
	}

	private int indexAtPosition(int x, int y) {
		Rectangle rect = getRect();
		if (!rect.contains(x, y)) {
			return -1;
		}
		int pos = y - rect.y() - 1;
		if (pos > -1 && pos < items.size()) {
			MenuItem i = items.get(pos);
			if (i != null) {
				return pos;
			}
		}
		return -1;
	}

	/**
	 * Specifies how a {@link MenuItem} shows selection state.
	 */
	public enum MenuItemCheckStyle {

		/**
		 * The menu item will be shown normally, with no check indicator. The default.
		 */
		NOCHECK,

		/**
		 * The menu item will indicate checked/un-checked state.
		 */
		CHECKED,

		/**
		 * The menu item is part of a menu radio group and will indicate selected state.
		 */
		RADIO
	}

	/**
	 * {@link MenuItem} represents an item in a {@link MenuView}.
	 *
	 * @see Menu
	 */
	public static class MenuItem  {

		private String title;
		private MenuItemCheckStyle checkStyle = MenuItemCheckStyle.NOCHECK;
		private List<MenuItem> items;

		public MenuItem(String title) {
			this(title, new MenuItem[0]);
		}

		protected MenuItem(String title, MenuItem[] items) {
			this(title, Arrays.asList(items));
		}

		protected MenuItem(String title, List<MenuItem> items) {
			this.title = title;
			this.items = items;
		}

		public static MenuItem of(String title) {
			return new MenuItem(title);
		}

		public String getTitle() {
			return title;
		}

		public MenuItemCheckStyle getCheckStyle() {
			return checkStyle;
		}

		public List<MenuItem> getItems() {
			return items;
		}
	}

	/**
	 * {@link Menu} represents an item in a {@link MenuView} being a specialisation
	 * of {@link MenuItem} indicating it having a sub-menu.
	 *
	 * @see MenuItem
	 */
	public static class Menu extends MenuItem {

		public Menu(String title) {
			super(title);
		}

		public Menu(String title, MenuItem[] items) {
			super(title, items);
		}

		public Menu(String title, List<MenuItem> items) {
			super(title, items);
		}

		public static Menu of(String title, MenuItem... items) {
			return new Menu(title, items);
		}
	}

	public record MenuViewAction(String action, View view) implements ViewAction {
	}

	public record MenuViewItemAction(MenuItem menuItem, String action, View view) implements ViewAction {
	}

}
