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
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.shell.component.view.event.KeyEvent;
import org.springframework.shell.component.view.geom.Rectangle;
import org.springframework.shell.component.view.screen.Screen;
import org.springframework.shell.component.view.screen.Screen.Writer;

/**
 * {@link ListView} shows {@link ListItem items} vertically.
 *
 * @author Janne Valkealahti
 */
public class ListView extends BoxView {

	private final static Logger log = LoggerFactory.getLogger(ListView.class);

	private final List<ListItem> items = new ArrayList<>();

	@Override
	protected void drawInternal(Screen screen) {
		Rectangle rect = getInnerRect();
		Writer writer = screen.writerBuilder().build();
		int y = rect.y();
		for (ListItem i : items) {
			writer.text(i.getTitle(), rect.x(), y++);
		}
		super.drawInternal(screen);
	}

	@Override
	public BiConsumer<KeyEvent, Consumer<View>> getInputHandler() {
		return (event, focus) -> {
			if (event.key() == null) {
				String data = event.data();
			}
			else {
				switch (event.key()) {
					case ENTER:
						log.debug("XXX ENTER");
					break;
					case UP:
						log.debug("XXX UP");
						break;
					case DOWN:
						log.debug("XXX DOWN");
						break;
					default:
						break;
				}
			}
			super.getInputHandler().accept(event, focus);
		};
	}

	public void setItems(List<ListItem> items) {
		this.items.clear();
		this.items.addAll(items);
	}

	public static class ListItem {

		private String title;

		public ListItem(String title) {
			this.title = title;
		}

		public String getTitle() {
			return title;
		}

	}

}
