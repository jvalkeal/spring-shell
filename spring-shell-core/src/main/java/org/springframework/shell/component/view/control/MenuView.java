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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jline.terminal.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.shell.component.view.event.EventLoop;
import org.springframework.shell.component.view.event.KeyEvent;
import org.springframework.shell.component.view.event.KeyHandler;
import org.springframework.shell.component.view.event.MouseHandler;
import org.springframework.shell.component.view.event.KeyEvent.KeyType;
import org.springframework.shell.component.view.geom.Rectangle;
import org.springframework.shell.component.view.message.ShellMessageBuilder;
import org.springframework.shell.component.view.screen.Color;
import org.springframework.shell.component.view.screen.Screen;
import org.springframework.shell.component.view.screen.ScreenItem;
import org.springframework.shell.component.view.screen.Screen.Writer;

/**
 * {@link MenuView} shows {@link MenuView} items vertically and is
 * typically used in layouts which builds complete terminal UI's.
 *
 * @author Janne Valkealahti
 */
public class MenuView extends BoxView {

	private final Logger log = LoggerFactory.getLogger(MenuView.class);
	private final List<MenuItem> items = new ArrayList<>();
	private MenuItem selected = null;

	/**
	 * Construct menu view with no initial menu items.
	 */
	public MenuView() {
		this(null);
	}

	/**
	 * Construct menu view with menu items.
	 *
	 * @param items the menu items
	 */
	public MenuView(@Nullable List<MenuItem> items) {
		setItems(items);
		init();
	}

	private void init() {
		addCommand(ViewCommand.LINE_UP, () -> move(-1));
		addCommand(ViewCommand.LINE_DOWN, () -> move(1));
		addKeyBinding(KeyType.UP, ViewCommand.LINE_UP);
		addKeyBinding(KeyType.DOWN, ViewCommand.LINE_DOWN);
	}

	Map<String, Runnable> viewCommands = new HashMap<>();
	private void addCommand(String viewCommand, Runnable runnable) {
		viewCommands.put(viewCommand, runnable);
	}

	Map<KeyType, String> viewBindings = new HashMap<>();
	private void addKeyBinding(KeyType keyType, String viewCommand) {
		viewBindings.put(keyType, viewCommand);
	}

	private void move(int count) {
		log.trace("move({})", count);
	}

	private void run(String command) {
		Runnable runnable = viewCommands.get(command);
		if (runnable != null) {
			Message<Runnable> message = ShellMessageBuilder.withPayload(runnable).setEventType(EventLoop.Type.TASK).build();
			dispatch(message);
		}
	}

	@Override
	protected void drawInternal(Screen screen) {
		Rectangle rect = getInnerRect();
		int y = rect.y();
		Writer writer = screen.writerBuilder().layer(getLayer()).build();
		Writer writer2 = screen.writerBuilder().layer(getLayer()).color(Color.WHITE).style(ScreenItem.STYLE_ITALIC).build();
		for (MenuItem item : items) {
			if (item == selected) {
				writer2.text(item.getTitle(), rect.x(), y);
			}
			else {
				writer.text(item.getTitle(), rect.x(), y);
			}
			y++;
		}
		super.drawInternal(screen);
	}

	@Override
	public KeyHandler getKeyHandler() {
		log.trace("getKeyHandler()");

		KeyHandler handler = args -> {
			KeyEvent event = args.event();
			boolean consumed = true;
			KeyType key = event.key();
			if (key != null) {
				String command = viewBindings.get(key);
				run(command);
			}
			return KeyHandler.resultOf(event, consumed, null);
		};

		return handler;
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

				MenuItem itemAt = itemAt(x, y);
				if (itemAt != null) {
					selected = itemAt;
				}
				log.info("XXX itemAt2 {} {} {}", x, y, itemAt);
			}
			return MouseHandler.resultOf(args.event(), true, view, null);
		};

		// return super.getMouseHandler();
	}

	/**
	 * Sets a new menu items. Will always clear existing items and if {@code null}
	 * is passed this effectively keeps items empty.
	 *
	 * @param items the menu items
	 */
	public void setItems(@Nullable List<MenuItem> items) {
		this.items.clear();
		selected = null;
		if (items != null) {
			this.items.addAll(items);
			if (!items.isEmpty()) {
				selected = items.get(0);
			}
		}
	}

	private MenuItem itemAt(int x, int y) {
		Rectangle rect = getRect();
		if (!rect.contains(x, y)) {
			return null;
		}
		int pos = y - rect.y();
		if (pos > -1 && pos < items.size()) {
			return items.get(pos);
		}
		return null;
	}

	public static class MenuItem  {

		private String title;
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

		public String getTitle() {
			return title;
		}

		public List<MenuItem> getItems() {
			return items;
		}
	}

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
	}

	void xxx() {
		// commands LineUp LineDown
		// events SelectionChanged
		dispatch(ShellMessageBuilder.ofView(this, new MenuViewAction("OpenSelectedItem", this)));
	}

	public record MenuViewAction(String action, View view) implements ViewAction {
	}

}
