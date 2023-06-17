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

import java.util.EnumSet;

import org.assertj.core.api.AssertProvider;
import org.jline.terminal.MouseEvent;
import org.junit.jupiter.api.BeforeEach;

import org.springframework.shell.component.view.ScreenAssert;
import org.springframework.shell.component.view.event.KeyEvent;
import org.springframework.shell.component.view.event.KeyHandler;
import org.springframework.shell.component.view.screen.DefaultScreen;
import org.springframework.shell.component.view.screen.Screen;

public class AbstractViewTests {

	protected Screen screen24x80;
	protected Screen screen7x10;
	protected Screen screen10x10;
	protected Screen screen0x0;

	@BeforeEach
	void setupScreens() {
		screen24x80 = new DefaultScreen(24, 80);
		screen7x10 = new DefaultScreen(7, 10);
		screen0x0 = new DefaultScreen();
		screen10x10 = new DefaultScreen(10, 10);
	}

	protected void dispatchEvent(View view, KeyEvent event) {
		KeyHandler keyHandler = view.getKeyHandler();
		if (keyHandler != null) {
			keyHandler.handle(new KeyHandler.KeyHandlerArgs(event));
		}
	}

	protected AssertProvider<ScreenAssert> forScreen(Screen screen) {
		return () -> new ScreenAssert(screen);
	}

	protected MouseEvent mouseClick(int x, int y) {
		return new MouseEvent(MouseEvent.Type.Released, MouseEvent.Button.Button1,
				EnumSet.noneOf(MouseEvent.Modifier.class), x, y);
	}

	protected MouseEvent mouseWheelUp(int x, int y) {
		return new MouseEvent(MouseEvent.Type.Wheel, MouseEvent.Button.WheelUp,
				EnumSet.noneOf(MouseEvent.Modifier.class), x, y);
	}

	protected MouseEvent mouseWheelDown(int x, int y) {
		return new MouseEvent(MouseEvent.Type.Wheel, MouseEvent.Button.WheelDown,
				EnumSet.noneOf(MouseEvent.Modifier.class), x, y);
	}
}
