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

import org.jline.terminal.MouseEvent;
import org.junit.jupiter.api.Test;

import org.springframework.shell.component.view.control.MenuView.MenuItem;
import org.springframework.shell.component.view.event.MouseHandler;
import org.springframework.shell.component.view.event.MouseHandler.MouseHandlerResult;

import static org.assertj.core.api.Assertions.assertThat;

class MenuViewTests extends AbstractViewTests {

	@Test
	void hasBorder() {
		MenuItem menuItem = new MenuView.MenuItem("sub1");
		MenuView view = new MenuView(Arrays.asList(menuItem));
		view.setShowBorder(true);
		view.setRect(0, 0, 80, 24);
		view.draw(screen24x80);
		assertThat(forScreen(screen24x80)).hasBorder(0, 0, 80, 24);
	}

	@Test
	void mouseClickXXX() {
		MenuItem menuItem = new MenuView.MenuItem("sub1");
		MenuView view = new MenuView(Arrays.asList(menuItem));
		// MenuItem menuItem = new MenuView.MenuItem("sub1");
		// MenuBarItem menuBarItem = new MenuBarView.MenuBarItem("menu1", new MenuView.MenuItem[]{menuItem});
		// MenuBarView view = new MenuBarView(new MenuBarView.MenuBarItem[]{menuBarItem});
		view.setRect(0, 0, 10, 10);

		MouseEvent click = mouseClick(0, 0);
		MouseHandlerResult result = view.getMouseHandler().handle(MouseHandler.argsOf(click));
		assertThat(result).isNotNull().satisfies(r -> {
			assertThat(r.event()).isEqualTo(click);
			assertThat(r.consumed()).isTrue();
			assertThat(r.focus()).isEqualTo(view);
			// assertThat(r.capture()).isEqualTo(view);
		});

		// MenuView menuView = (MenuView) ReflectionTestUtils.getField(view, "menuView");
		// assertThat(menuView).isNotNull();
	}

}
