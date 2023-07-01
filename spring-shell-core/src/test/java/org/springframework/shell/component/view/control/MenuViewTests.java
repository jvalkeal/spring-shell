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

import java.time.Duration;
import java.util.Arrays;

import org.jline.terminal.MouseEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import org.springframework.shell.component.view.control.MenuView.MenuItem;
import org.springframework.shell.component.view.control.MenuView.MenuViewAction;
import org.springframework.shell.component.view.control.MenuView.MenuViewItemAction;
import org.springframework.shell.component.view.event.KeyEvent.KeyType;
import org.springframework.shell.component.view.event.MouseHandler.MouseHandlerResult;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class MenuViewTests extends AbstractViewTests {

	private static final String SELECTED_FIELD = "activeItemIndex";

	@Test
	void constructView() {
		MenuView view;
		view = new MenuView(new MenuItem[] {
			MenuItem.of("sub1"),
			MenuItem.of("sub2")
		});
		assertThat(view.getItems()).hasSize(2);
		view = new MenuView(new MenuItem[] {
			new MenuItem("sub1"),
			new MenuItem("sub2")
		});
		assertThat(view.getItems()).hasSize(2);
	}

	@Nested
	class Styling {

		@Test
		void hasBorder() {
			MenuItem menuItem = new MenuView.MenuItem("sub1");
			MenuView view = new MenuView(Arrays.asList(menuItem));
			view.setShowBorder(true);
			view.setRect(0, 0, 80, 24);
			view.draw(screen24x80);
			assertThat(forScreen(screen24x80)).hasBorder(0, 0, 80, 24);
		}

	}

	@Nested
	class Selection {

		MenuView view;

		@BeforeEach
		void setup() {
			view = new MenuView(new MenuItem[] {
				MenuItem.of("sub1"),
				MenuItem.of("sub2")
			});
			configure(view);
			view.setRect(0, 0, 10, 10);
		}

		@Test
		void firstItemShouldAlwaysBeSelected() {
			MenuItem menuItem = new MenuView.MenuItem("sub1");
			MenuView view = new MenuView(Arrays.asList(menuItem));
			Integer selected = (Integer) ReflectionTestUtils.getField(view, SELECTED_FIELD);
			assertThat(selected).isEqualTo(0);
		}

		@Test
		void clickInItemSelects() {
			handleMouseClick(view, 0, 2);
			Integer selected = (Integer) ReflectionTestUtils.getField(view, SELECTED_FIELD);
			assertThat(selected).isEqualTo(1);
		}

		@Test
		void downArrowMovesSelection() {
			Integer selected;

			handleKey(view, KeyType.DOWN);
			selected = (Integer) ReflectionTestUtils.getField(view, SELECTED_FIELD);
			assertThat(selected).isEqualTo(1);
		}

		void upArrowMovesSelection() {
		}

		void wheelMovesSelection() {
		}

		void selectionShouldNotMoveOutOfBounds() {
		}

		void canSelectManually() {

		}
	}

	@Nested
	class Events {

		MenuView view;

		@BeforeEach
		void setup() {
			view = new MenuView(new MenuItem[] {
				MenuItem.of("sub1"),
				MenuItem.of("sub2")
			});
			configure(view);
			view.setRect(0, 0, 10, 10);
		}

		@Test
		void handlesMouseClickInItem() {
			MouseEvent click = mouseClick(0, 2);

			Flux<MenuViewItemAction> actions = eventLoop.viewEvents(MenuViewItemAction.class)
				.filter(a -> ViewCommand.SELECTION_CHANGED.equals(a.action()));
			StepVerifier verifier = StepVerifier.create(actions)
				.expectNextCount(1)
				.thenCancel()
				.verifyLater();

			MouseHandlerResult result = handleMouseClick(view, click);

			assertThat(result).isNotNull().satisfies(r -> {
				assertThat(r.event()).isEqualTo(click);
				assertThat(r.consumed()).isTrue();
				assertThat(r.focus()).isEqualTo(view);
				assertThat(r.capture()).isEqualTo(view);
			});
			verifier.verify(Duration.ofSeconds(1));
		}

		@Test
		void keySelectSendsEvent() {
			Flux<MenuViewAction> actions = eventLoop.viewEvents(MenuViewAction.class)
				.filter(a -> ViewCommand.OPEN_SELECTED_ITEM.equals(a.action()));
			StepVerifier verifier = StepVerifier.create(actions)
				.expectNextCount(1)
				.thenCancel()
				.verifyLater();

			handleKey(view, KeyType.ENTER);
			verifier.verify(Duration.ofSeconds(1));
		}

		@Test
		void selectionChangedSendsEvent() {
			Flux<MenuViewItemAction> actions = eventLoop.viewEvents(MenuViewItemAction.class)
				.filter(a -> ViewCommand.SELECTION_CHANGED.equals(a.action()));
			StepVerifier verifier = StepVerifier.create(actions)
				.expectNextCount(1)
				.thenCancel()
				.verifyLater();

			handleKey(view, KeyType.DOWN);
			verifier.verify(Duration.ofSeconds(1));
		}

	}

}
