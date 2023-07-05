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

import org.jline.terminal.MouseEvent;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.shell.component.view.control.MenuBarView.MenuBarItem;
import org.springframework.shell.component.view.control.MenuView.MenuItem;
import org.springframework.shell.component.view.event.MouseHandler;
import org.springframework.shell.component.view.event.MouseHandler.MouseHandlerResult;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class MenuBarViewTests extends AbstractViewTests {

	@Nested
	class Styling {

		@Test
		void hasBorder() {
			MenuBarView view = new MenuBarView(new MenuBarView.MenuBarItem[0]);
			view.setShowBorder(true);
			view.setRect(0, 0, 80, 24);
			view.draw(screen24x80);
			assertThat(forScreen(screen24x80)).hasBorder(0, 0, 80, 24);
		}

	}

	@Nested
	class Menus {

		@Test
		void mouseClicksSameOpensAndClosesMenu() {
			MenuItem menuItem = new MenuView.MenuItem("sub1");
			MenuBarItem menuBarItem = new MenuBarView.MenuBarItem("menu1", new MenuView.MenuItem[]{menuItem});
			MenuBarView view = new MenuBarView(new MenuBarView.MenuBarItem[]{menuBarItem});
			configure(view);
			view.setRect(0, 0, 10, 10);

			MouseEvent click1 = mouseClick(0, 0);
			MouseHandlerResult result1 = view.getMouseHandler().handle(MouseHandler.argsOf(click1));
			assertThat(result1).isNotNull().satisfies(r -> {
				assertThat(r.event()).isEqualTo(click1);
				assertThat(r.consumed()).isTrue();
				assertThat(r.focus()).isEqualTo(view);
				assertThat(r.capture()).isEqualTo(view);
			});

			MenuView menuView1 = (MenuView) ReflectionTestUtils.getField(view, "currentMenuView");
			assertThat(menuView1).isNotNull();

			MouseEvent click2 = mouseClick(0, 0);
			MouseHandlerResult result2 = view.getMouseHandler().handle(MouseHandler.argsOf(click2));
			assertThat(result2).isNotNull().satisfies(r -> {
				assertThat(r.event()).isEqualTo(click2);
				assertThat(r.consumed()).isTrue();
				assertThat(r.focus()).isEqualTo(view);
				assertThat(r.capture()).isEqualTo(view);
			});

			MenuView menuView2 = (MenuView) ReflectionTestUtils.getField(view, "currentMenuView");
			assertThat(menuView2).isNull();

		}

		@Test
		void menuHasPositionRelativeToHeader() {
			MenuBarView view = new MenuBarView(new MenuBarItem[] {
				new MenuBarItem("menu1", new MenuItem[] {
					new MenuItem("sub11")
				}),
				new MenuBarItem("menu2", new MenuItem[] {
					new MenuItem("sub21")
				})
			});
			configure(view);
			view.setRect(0, 0, 20, 1);

			MouseEvent click = mouseClick(7, 0);
			MouseHandlerResult result = view.getMouseHandler().handle(MouseHandler.argsOf(click));
			assertThat(result).isNotNull().satisfies(r -> {
				assertThat(r.event()).isEqualTo(click);
				assertThat(r.consumed()).isTrue();
				assertThat(r.focus()).isEqualTo(view);
				assertThat(r.capture()).isEqualTo(view);
			});

			MenuView menuView = (MenuView) ReflectionTestUtils.getField(view, "currentMenuView");
			assertThat(menuView).isNotNull().satisfies(m -> {
				assertThat(m.getRect()).satisfies(r -> {
					assertThat(r.x()).isEqualTo(7);
				});
			});

		}

	}

}