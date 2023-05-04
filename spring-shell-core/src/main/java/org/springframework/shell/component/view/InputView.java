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

import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * @author Janne Valkealahti
 */
public class InputView extends BoxView {

	private final static Logger log = LoggerFactory.getLogger(InputView.class);
	private String text = "";

	@Override
	protected void drawInternal(Screen screen) {
		Rectangle rect = getRect();
		if (text != null) {
			screen.print(text, rect.x(), rect.y(), text.length());
			screen.setShowCursor(true);
			screen.setCursorPosition(new Position(rect.x() + text.length(), rect.y()));
		}
	}

	@Override
	public BiFunction<KeyEvent, Consumer<View>, KeyEvent> getInputHandler() {
		return (event, view) -> {
			log.debug("KeyEvent {}", event);
			text = text + event.binding();
			return event;
		};
	}
}
