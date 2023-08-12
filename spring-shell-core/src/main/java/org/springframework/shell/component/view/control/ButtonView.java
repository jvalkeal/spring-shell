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

import org.springframework.shell.component.view.event.KeyEvent.Key;
import org.springframework.shell.component.view.event.KeyHandler;
import org.springframework.shell.component.view.event.MouseEvent;
import org.springframework.shell.component.view.event.MouseHandler;
import org.springframework.shell.component.view.message.ShellMessageBuilder;
import org.springframework.shell.component.view.screen.Screen;

public class ButtonView extends BoxView {

	private String text;
	private Runnable action;

	public ButtonView() {
		this(null, null);
	}

	public ButtonView(String text) {
		this(text, null);
	}

	public ButtonView(String text, Runnable action) {
		this.text = text;
		this.action = action;
	}

	@Override
	protected void initInternal() {
		registerKeyBinding(Key.Enter, () -> keySelect());
		registerMouseBinding(MouseEvent.Type.Released | MouseEvent.Button.Button1, event -> mouseSelect(event));
	}

	@Override
	public KeyHandler getKeyHandler() {
		return super.getKeyHandler();
	}

	@Override
	public MouseHandler getMouseHandler() {
		return super.getMouseHandler();
	}

	@Override
	protected void drawInternal(Screen screen) {
		super.drawInternal(screen);
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setAction(Runnable action) {
		this.action = action;
	}

	private void keySelect() {
	}

	private void mouseSelect(MouseEvent event) {
	}

	void xxx() {
		dispatch(ShellMessageBuilder.ofView(this, ButtonViewSelectEvent.of(this)));
	}


	public record ButtonViewItemEventArgs() implements ViewEventArgs {

		public static ButtonViewItemEventArgs of() {
			return new ButtonViewItemEventArgs();
		}
	}

	public record ButtonViewSelectEvent(View view, ButtonViewItemEventArgs args) implements ViewEvent {

		public static ButtonViewSelectEvent of(View view) {
			return new ButtonViewSelectEvent(view, ButtonViewItemEventArgs.of());
		}
	}

}
