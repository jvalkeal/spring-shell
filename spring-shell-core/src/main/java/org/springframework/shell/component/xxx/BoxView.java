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
package org.springframework.shell.component.xxx;

import org.springframework.util.StringUtils;

/**
 * {@code BoxView} is a {@link View} with an empty background and optional
 * border and title. All "boxed" views can use this as their base
 * implementation by either subclassing or wrapping.
 *
 * @author Janne Valkealahti
 */
public class BoxView extends AbstractView {

	private String title = null;
	private boolean showBorder = false;

	public void drawInternal(VirtualDisplay display) {
		if (getWidth() <= 0 || getHeight() <= 0) {
			return;
		}

		if (showBorder && getWidth() >= 2 && getHeight() >= 2) {

			for (int i = getX() + 1; i < getX() + getWidth() - 1; i++) {
				display.setContent(i, getY(), '─');
				display.setContent(i, getY() + getHeight() - 1, '─');
			}

			for (int i = getY() + 1; i < getY() + getHeight() - 1; i++) {
				display.setContent(getX(), i, '│');
				display.setContent(getX() + getWidth() - 1, i, '│');
			}

			display.setContent(getX(), getY(), '┌');
			display.setContent(getX() + getWidth() - 1, getY(), '┐');
			display.setContent(getX(), getY() + getHeight() - 1, '└');
			display.setContent(getX() + getWidth() - 1, getY() + getHeight() - 1, '┘');

			if (StringUtils.hasText(title)) {
				display.print(title, getX() + 1, getY(), getWidth() - 2);
			}
		}

		if (getDrawFunction() != null) {
			Rectangle rect = new Rectangle(getX(), getY(), getWidth(), getHeight());
			rect = getDrawFunction().apply(display, rect);
		}
	}

	public boolean isShowBorder() {
		return showBorder;
	}

	public void setShowBorder(boolean showBorder) {
		this.showBorder = showBorder;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
