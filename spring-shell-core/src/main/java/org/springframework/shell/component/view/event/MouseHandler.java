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
package org.springframework.shell.component.view.event;

import org.jline.terminal.MouseEvent;

import org.springframework.lang.Nullable;
import org.springframework.shell.component.view.View;

/**
 * Handles mouse events in a form of {@link MouseHandlerArgs} and returns
 * {@link MouseHandlerResult}.
 *
 * @author Janne Valkealahti
 */
@FunctionalInterface
public interface MouseHandler {

	/**
	 * Handle mouse event wrapped in a {@link MouseHandlerArgs}.
	 *
	 * @param args the mouse handler arguments
	 * @return a handler result
	 */
	MouseHandlerResult handle(MouseHandlerArgs args);

	/**
	 * Construct {@link MouseHandlerArgs} from a {@link MouseEvent}.
	 *
	 * @param event the mouse event
	 * @return a mouse handler args
	 */
	static MouseHandlerArgs argsOf(MouseEvent event) {
		return new MouseHandlerArgs(event);
	}

	/**
	 * Construct {@link MouseHandlerResult} from a {@link MouseEvent} and a
	 * {@link View}.
	 *
	 * @param event the mouse event
	 * @param view  the view
	 * @return a mouse handler result
	 */
	static MouseHandlerResult resultOf(MouseEvent event, View view) {
		return new MouseHandlerResult(event, false, view);
	}

	/**
	 * Arguments for a {@link MouseHandler}.
	 *
	 * @param event the mouse event
	 */
	record MouseHandlerArgs(MouseEvent event) {
	}

	/**
	 * Result from a {@link MouseHandler}.
	 *
	 * @param event the mouse event
	 * @param consumed flag telling if event was consumed
	 * @param view the view which consumed an event
	 */
	record MouseHandlerResult(@Nullable MouseEvent event, boolean consumed, @Nullable View view) {
	}
}
