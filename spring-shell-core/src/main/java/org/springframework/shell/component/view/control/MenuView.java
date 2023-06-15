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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.shell.component.view.geom.Rectangle;
import org.springframework.shell.component.view.screen.Screen;
import org.springframework.shell.component.view.screen.Screen.Writer;

/**
 * {@link MenuView} shows a menu.
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
		Writer writer = screen.writerBuilder().build();
		for (MenuItem item : items) {
			writer.text(item.getTitle(), rect.x(), y);
			y++;
		}
		super.drawInternal(screen);
	}

	public static class MenuItem  {

		private String title;
		private List<MenuItem> items;
		public MenuItem(String title) {
			this(title, null);
		}
		public MenuItem(String title, MenuItem[] items) {
			this.title = title;
			this.items = Arrays.asList(items);
		}
		public String getTitle() {
			return title;
		}
	}

	public static class Menu extends MenuItem {

		public Menu(String title) {
			super(title);
		}
		public Menu(String title, MenuItem[] items) {
			super(title, items);
		}

	}

}
