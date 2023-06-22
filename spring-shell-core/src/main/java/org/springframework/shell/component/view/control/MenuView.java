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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.shell.component.view.event.KeyHandler;
import org.springframework.shell.component.view.event.MouseHandler;
import org.springframework.shell.component.view.geom.Rectangle;
import org.springframework.shell.component.view.message.ShellMessageBuilder;
import org.springframework.shell.component.view.screen.Screen;
import org.springframework.shell.component.view.screen.Screen.Writer;

/**
 * {@link MenuView} shows {@link MenuView} items vertically and is
 * typically used in layouts which builds complete terminal UI's.
 *
 * @author Janne Valkealahti
 */
public class MenuView extends BoxView {

	private final Logger log = LoggerFactory.getLogger(MenuView.class);
	private final List<MenuItem> items;

	public MenuView(List<MenuItem> items) {
		this.items = items;
	}

	@Override
	protected void drawInternal(Screen screen) {
		Rectangle rect = getInnerRect();
		int y = rect.y();
		Writer writer = screen.writerBuilder().layer(getLayer()).build();
		for (MenuItem item : items) {
			writer.text(item.getTitle(), rect.x(), y);
			y++;
		}
		super.drawInternal(screen);
	}

	@Override
	public KeyHandler getKeyHandler() {
		log.trace("getKeyHandler()");
		return super.getKeyHandler();
	}

	@Override
	public MouseHandler getMouseHandler() {
		log.trace("getMouseHandler()");
		// return args -> {
		// 	View view = null;
		// 	MouseEvent event = args.event();
		// 	if (event.getModifiers().isEmpty() && event.getType() == MouseEvent.Type.Released
		// 			&& event.getButton() == MouseEvent.Button.Button1) {
		// 		int x = event.getX();
		// 		int y = event.getY();
		// 		if (getInnerRect().contains(x, y)) {
		// 			view = this;
		// 		}

		// 		MenuBarItem itemAt = itemAt(x, y);
		// 		log.info("XXX itemAt {} {} {}", x, y, itemAt);
		// 		if (itemAt != null) {
		// 			if (this.itemAt == itemAt) {
		// 				this.menuView = null;
		// 				this.itemAt = null;
		// 			}
		// 			else {
		// 				MenuView menuView = new MenuView(itemAt.getItems());
		// 				menuView.setShowBorder(true);
		// 				menuView.setBackgroundColor(Color.AQUAMARINE4);
		// 				menuView.setLayer(1);
		// 				Rectangle rect = getInnerRect();
		// 				menuView.setRect(rect.x(), rect.y()+1, 15, 10);
		// 				this.menuView = menuView;
		// 				this.itemAt = itemAt;
		// 			}
		// 		}
		// 	}
		// 	return MouseHandler.resultOf(args.event(), true, view, null);
		// };

		return super.getMouseHandler();
	}

	public static class MenuItem  {

		private String title;
		private List<MenuItem> items;
		private boolean enabled = true;
		private boolean checked;

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
		dispatch(ShellMessageBuilder.ofView(this, new MenuViewAction("LineDown", this)));
	}

	public record MenuViewAction(String action, View view) implements ViewAction {
	}

}
