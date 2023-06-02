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

import org.springframework.shell.component.view.geom.Rectangle;
import org.springframework.shell.component.view.screen.Screen;

/**
 * {@link AppView} provides an opinionated terminal UI application view
 * controlling main viewing area, menubar, statusbar and modal window system.
 *
 * @author Janne Valkealahti
 */
public class AppView extends BoxView {

	private View main;
	private View modal;

	@Override
	protected void drawInternal(Screen screen) {
		Rectangle rect = getInnerRect();
		if (main != null) {
			main.setRect(rect.x(), rect.y(), rect.width(), rect.height());
			main.draw(screen);
		}
		if (modal != null) {
			modal.setLayer(1);
			modal.setRect(rect.x() + 5, rect.y() + 5, rect.width() - 10, rect.height() - 10);
			modal.draw(screen);
		}
		super.drawInternal(screen);
	}

	public void setMain(View main) {
		this.main = main;
	}

	public void setModal(View modal) {
		this.modal = modal;
	}
}