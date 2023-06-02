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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.shell.component.view.geom.Rectangle;
import org.springframework.shell.component.view.screen.Screen;
import org.springframework.shell.component.view.screen.Screen.Writer;

/**
 * {@link SelectorListView} shows {@link SelectorItem items} vertically.
 *
 * @author Janne Valkealahti
 */
public class SelectorListView extends BoxView {

	private final Logger log = LoggerFactory.getLogger(SelectorListView.class);

	@Override
	protected void drawInternal(Screen screen) {
		Rectangle rect = getInnerRect();
		Writer writer = screen.writerBuilder().build();
		writer.text("item1", rect.x(), rect.y());
		writer.text("item1", rect.x(), rect.y() + 1);
		super.drawInternal(screen);
	}

	public static class SelectorItem {

		private String title;

		public SelectorItem(String title) {
			this.title = title;
		}

		public String getTitle() {
			return title;
		}

	}

}
