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

import org.jline.utils.AttributedStyle;

import org.springframework.shell.component.view.Screen.ScreenItem;
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
	private int innerX = -1;
	private int innerY;
	private int innerWidth;
	private int innerHeight;
	private int paddingTop;
	private int paddingBottom;
	private int paddingLeft;
	private int paddingRight;

	/**
	 *
	 * @param paddingTop
	 * @param paddingBottom
	 * @param paddingLeft
	 * @param paddingRight
	 * @return
	 */
	public BoxView setBorderPadding(int paddingTop, int paddingBottom, int paddingLeft, int paddingRight) {
		this.paddingTop = paddingTop;
		this.paddingBottom = paddingBottom;
		this.paddingLeft = paddingLeft;
		this.paddingRight = paddingRight;
		return this;
	}

	@Override
	public void setRect(int x, int y, int width, int height) {
		this.innerX = -1;
		super.setRect(x, y, width, height);
	}

	/**
	 *
	 * @param showBorder
	 */
	public void setShowBorder(boolean showBorder) {
		this.showBorder = showBorder;
	}

	/**
	 *
	 * @return
	 */
	public boolean isShowBorder() {
		return showBorder;
	}

	/**
	 *
	 * @param title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	protected void drawInternal(Screen screen) {
		Rectangle rect = getRect();
		if (rect.width() <= 0 || rect.height() <= 0) {
			return;
		}

		if (showBorder && rect.width() >= 2 && rect.height() >= 2) {

			for (int i = rect.x() + 1; i < rect.y() + rect.width() - 1; i++) {
				screen.setContent(i, rect.y(), new ScreenItem(new String(new char[] { '═' }), AttributedStyle.DEFAULT.foreground(AttributedStyle.RED)));
				screen.setContent(i, rect.y() + rect.height() - 1, new ScreenItem(new String(new char[] { '═' }), null));
			}

			for (int i = rect.y() + 1; i < rect.y() + rect.height() - 1; i++) {
				screen.setContent(rect.x(), i, new ScreenItem(new String(new char[] { '║' }), null));
				screen.setContent(rect.x() + rect.width() - 1, i, new ScreenItem(new String(new char[] { '║' }), null));
			}

			screen.setContent(rect.x(), rect.y(), new ScreenItem(new String(new char[] { '┌' }), null));
			screen.setContent(rect.x() + rect.width() - 1, rect.y(), new ScreenItem(new String(new char[] { '┐' }), null));
			screen.setContent(rect.x(), rect.y() + rect.height() - 1, new ScreenItem(new String(new char[] { '└' }), null));
			screen.setContent(rect.x() + rect.width() - 1, rect.y() + rect.height() - 1, new ScreenItem(new String(new char[] { '┘' }), null));

			if (StringUtils.hasText(title)) {
				screen.print(title, rect.x() + 1, rect.y(), rect.width() - 2);
			}
		}

		if (getDrawFunction() != null) {
			Rectangle r = getDrawFunction().apply(screen, rect);
			innerX = r.x();
			innerY = r.y();
			innerWidth = r.width();
			innerHeight = r.height();
		}
		else {
			Rectangle r = getInnerRect();
			innerX = r.x();
			innerY = r.y();
			innerWidth = r.width();
			innerHeight = r.height();
		}
	}

	/**
	 *
	 *
	 * @return
	 */
	protected Rectangle getInnerRect() {
		if (innerX >= 0) {
			return new Rectangle(innerX, innerY, innerWidth, innerHeight);
		}
		Rectangle rect = getRect();
		int x = rect.x();
		int y = rect.y();
		int width = rect.width();
		int height = rect.height();
		if (isShowBorder()) {
			x++;
			y++;
			width -= 2;
			height -= 2;
		}
		x = x + paddingLeft;
		y = y + paddingTop;
		width = width - paddingLeft - paddingRight;
		height = height - paddingTop - paddingBottom;
		if (width < 0) {
			width = 0;
		}
		if (height < 0) {
			height = 0;
		}
		return new Rectangle(x, y, width, height);
	}
}
