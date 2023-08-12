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

import java.util.List;

import org.springframework.shell.component.view.event.KeyHandler;
import org.springframework.shell.component.view.event.MouseHandler;
import org.springframework.shell.component.view.message.ShellMessageBuilder;
import org.springframework.shell.component.view.screen.Screen;

public class DialogView extends BoxView {

	private GridView grid;
	private View content;
	private List<ButtonView> buttons;

	public DialogView() {
	}

	@Override
	protected void initInternal() {
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

	private void initLayout() {
		grid = new GridView();
		grid.setRowSize(0, 1);
		grid.setColumnSize(0);

	}

	void xxx() {
		dispatch(ShellMessageBuilder.ofView(this, DialogViewCloseEvent.of(this)));
	}

	public record DialogViewItemEventArgs() implements ViewEventArgs {

		public static DialogViewItemEventArgs of() {
			return new DialogViewItemEventArgs();
		}
	}

	public record DialogViewCloseEvent(View view, DialogViewItemEventArgs args) implements ViewEvent {

		public static DialogViewCloseEvent of(View view) {
			return new DialogViewCloseEvent(view, DialogViewItemEventArgs.of());
		}
	}

}
