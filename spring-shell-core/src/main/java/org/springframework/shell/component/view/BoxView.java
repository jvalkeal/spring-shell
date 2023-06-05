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

import org.jline.terminal.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.shell.component.view.event.MouseHandler;
import org.springframework.shell.component.view.geom.HorizontalAlign;
import org.springframework.shell.component.view.geom.Rectangle;
import org.springframework.shell.component.view.geom.VerticalAlign;
import org.springframework.shell.component.view.screen.Screen;
import org.springframework.util.StringUtils;

/**
 * {@code BoxView} is a {@link View} with an empty background and optional
 * border and title. All "boxed" views can use this as their base
 * implementation by either subclassing or wrapping.
 *
 * @author Janne Valkealahti
 */
public class BoxView extends AbstractView {

	private final static Logger log = LoggerFactory.getLogger(BoxView.class);
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
	private int backgroundColor = -1;
	private int titleColor = -1;
	private int titleStyle = -1;
	private HorizontalAlign titleAlign;

	@Override
	public void setRect(int x, int y, int width, int height) {
		this.innerX = -1;
		super.setRect(x, y, width, height);
	}

	@Override
	public MouseHandler getMouseHandler() {
		return args -> {
			View view = null;
			MouseEvent event = args.event();
			if (event.getModifiers().isEmpty() && event.getType() == MouseEvent.Type.Released
					&& event.getButton() == MouseEvent.Button.Button1) {
				int x = event.getX();
				int y = event.getY();
				if (getRect().contains(x, y)) {
					view = this;
				}
			}
			return MouseHandler.resultOf(args.event(), view);
		};
	}

	/**
	 * Sets a paddings for this view.
	 *
	 * @param paddingTop the top padding
	 * @param paddingBottom the bottom padding
	 * @param paddingLeft the left padding
	 * @param paddingRight the right padding
	 * @return a BoxView for chaining
	 */
	public BoxView setBorderPadding(int paddingTop, int paddingBottom, int paddingLeft, int paddingRight) {
		this.paddingTop = paddingTop;
		this.paddingBottom = paddingBottom;
		this.paddingLeft = paddingLeft;
		this.paddingRight = paddingRight;
		return this;
	}

	/**
	 * Defines if border is shown.
	 *
	 * @param showBorder the flag showing border
	 */
	public void setShowBorder(boolean showBorder) {
		this.showBorder = showBorder;
	}

	/**
	 * Returns if border is shown.
	 *
	 * @return true if border is shown
	 */
	public boolean isShowBorder() {
		return showBorder;
	}

	/**
	 * Sets a title. {@code title} is shown within a top-level border boundary and
	 * will not be visible if border itself is not visible.
	 *
	 * @param title the border title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Sets a background color.
	 *
	 * @param backgroundColor the background color
	 */
	public void setBackgroundColor(int backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	/**
	 * Sets a title color.
	 *
	 * @param titleColor the title color
	 */
	public void setTitleColor(int titleColor) {
		this.titleColor = titleColor;
	}

	/**
	 * Sets a title style.
	 *
	 * @param titleStyle the title style
	 */
	public void setTitleStyle(int titleStyle) {
		this.titleStyle = titleStyle;
	}

	/**
	 * Sets a title align.
	 *
	 * @param titleAlign the title align
	 */
	public void setTitleAlign(HorizontalAlign titleAlign) {
		this.titleAlign = titleAlign;
	}

	/**
	 * Possibly draws a box around this view and title in a box top boundary. Also
	 * calls a {@code draw function} if defined.
	 *
	 * @param screen the screen
	 */
	protected void drawInternal(Screen screen) {
		Rectangle rect = getRect();
		if (rect.width() <= 0 || rect.height() <= 0) {
			return;
		}
		if (backgroundColor > -1) {
			screen.writerBuilder().layer(getLayer()).build().background(rect, backgroundColor);
			// screen.setBackground(rect, backgroundColor);
		}
		if (showBorder && rect.width() >= 2 && rect.height() >= 2) {
			screen.writerBuilder().layer(getLayer()).build().border(rect.x(), rect.y(), rect.width(), rect.height());
			// screen.printBorder(rect.x(), rect.y(), rect.width(), rect.height());
			if (StringUtils.hasText(title)) {
				Rectangle r = new Rectangle(rect.x() + 1, rect.y(), rect.width() - 2, 1);
				if (titleColor > -1) {
					screen.writerBuilder().layer(getLayer()).color(titleColor).style(titleStyle).build().text(title, r, titleAlign, VerticalAlign.TOP);
					// screen.print(title, r, titleAlign, VerticalAlign.TOP, titleColor, titleStyle);
				}
				else {
					screen.writerBuilder().layer(getLayer()).build().text(title, r, titleAlign, VerticalAlign.TOP);
					// screen.print(title, r, titleAlign, VerticalAlign.TOP);
				}
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
	 * Gets an inner rectangle of this view.
	 *
	 * @return an inner rectangle of this view
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
