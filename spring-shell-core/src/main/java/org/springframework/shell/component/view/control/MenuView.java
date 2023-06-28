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
	private MenuItem selected = null;

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

	// private Map<MouseBinding, String> mouseBindings = new HashMap<>();
	// private Map<String, Consumer<MouseEvent>> mouseCommands = new HashMap<>();

	// record MouseBinding(MouseEvent.Type type, MouseEvent.Button button, EnumSet<MouseEvent.Modifier> modifiers) {
	// }

	// protected void registerMouseConsumerCommand(String viewCommand, Consumer<MouseEvent> consumer) {
	// 	mouseCommands.put(viewCommand, consumer);
	// }

	// protected void registerMouseBinding(MouseEvent.Type type, MouseEvent.Button button, EnumSet<MouseEvent.Modifier> modifiers,
	// 		String viewCommand) {
	// 	mouseBindings.put(new MouseBinding(type, button, modifiers), viewCommand);
	// }

	// @Override
	// public MouseHandler getMouseHandler() {
	// 	log.trace("getMouseHandler()");
	// 	MouseHandler handler = args -> {
	// 		MouseEvent event = args.event();
	// 		log.info("XXX mouse binding event {}", event);

	// 		MouseEvent.Type type = event.getType();
	// 		MouseEvent.Button button = event.getButton();
	// 		EnumSet<MouseEvent.Modifier> modifiers = event.getModifiers();
	// 		MouseBinding b = new MouseBinding(type, button, modifiers);

	// 		// String string = mouseBindings.get(b);
	// 		// Consumer<MouseEvent> consumer = mouseCommands.get(string);
	// 		// consumer.accept(event);

	// 		// log.info("XXX mouse binding command {}", string);

	// 		return MouseHandler.resultOf(args.event(), true, null, null);
	// 	};
	// 	return handler;
	// 	// return args -> {
	// 	// 	View view = null;
	// 	// 	MouseEvent event = args.event();
	// 	// 	if (event.getModifiers().isEmpty() && event.getType() == MouseEvent.Type.Released
	// 	// 			&& event.getButton() == MouseEvent.Button.Button1) {
	// 	// 		int x = event.getX();
	// 	// 		int y = event.getY();
	// 	// 		if (getInnerRect().contains(x, y)) {
	// 	// 			view = this;
	// 	// 		}

	// 	// 		MenuItem itemAt = itemAt(x, y);
	// 	// 		if (itemAt != null) {
	// 	// 			selected = itemAt;
	// 	// 		}
	// 	// 		log.info("XXX itemAt2 {} {} {}", x, y, itemAt);
	// 	// 	}
	// 	// 	return MouseHandler.resultOf(args.event(), true, view, null);
	// 	// };
	// }

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

	@Override
	protected void initInternal() {
		registerRunnableCommand(ViewCommand.LINE_UP, () -> move(-1));
		registerRunnableCommand(ViewCommand.LINE_DOWN, () -> move(1));

		registerKeyBinding(KeyType.UP, ViewCommand.LINE_UP);
		registerKeyBinding(KeyType.DOWN, ViewCommand.LINE_DOWN);

		registerMouseBindingConsumerCommand(ViewCommand.SELECT, event -> select(event));

		registerMouseBinding(MouseEvent.Type.Released, MouseEvent.Button.Button1,
				EnumSet.noneOf(MouseEvent.Modifier.class), ViewCommand.SELECT);

	}

	private void move(int count) {
		log.trace("move({})", count);
	}

	private void select(MouseEvent event) {
		log.trace("select({})", event);
		int x = event.getX();
		int y = event.getY();
		MenuItem itemAt = itemAt(x, y);
		selected = itemAt;
	}

	private MenuItem itemAt(int x, int y) {
		Rectangle rect = getRect();
		if (!rect.contains(x, y)) {
			return null;
		}
		int pos = y - rect.y() - 1;
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
