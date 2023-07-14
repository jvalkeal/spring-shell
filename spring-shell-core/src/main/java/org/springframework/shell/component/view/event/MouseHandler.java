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

import java.util.function.Predicate;

import org.springframework.lang.Nullable;
import org.springframework.shell.component.view.control.View;

/**
 * Handles mouse events in a form of {@link MouseHandlerArgs} and returns
 * {@link MouseHandlerResult}. Typically used in a {@link View}.
 *
 * {@link MouseHandler} itself don't define any restrictions how it's used.
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
	 * Returns a composed handler that first handles the {@code other} and
	 * then handles this handler if {@code predicate} against result from
	 * {@code other} matches.
	 *
	 * @param other the handler to handle before this handler
	 * @param predicate the predicate test against results from other
	 * @return a composed handler
	 */
	default MouseHandler from(MouseHandler other, Predicate<MouseHandlerResult> predicate) {
		return args -> {
			MouseHandlerResult result = other.handle(args);
			if (predicate.test(result)) {
				return result;
			}
			return handle(args);
		};
    }

    default MouseHandler fromIfConsumed(MouseHandler other) {
		return from(other, result -> result.consumed());
    }

    default MouseHandler fromIfNotConsumed(MouseHandler other) {
		return from(other, result -> !result.consumed());
    }

	/**
	 * Construct {@link MouseHandlerArgs} from a {@link MouseEvent}.
	 *
	 * @param event the mouse event
	 * @return a mouse handler args
	 */
	static MouseHandlerArgs argsOf(MouseEventx event) {
		return new MouseHandlerArgs(event);
	}

	/**
	 * Arguments for a {@link MouseHandler}.
	 *
	 * @param event the mouse event
	 */
	record MouseHandlerArgs(MouseEventx event) {
	}

	/**
	 * Construct {@link MouseHandlerResult} from a {@link MouseEvent} and a
	 * {@link View}.
	 *
	 * @param event the mouse event
	 * @param consumed flag telling if event was consumed
	 * @param focus the view which is requesting focus
	 * @param capture the view which captured an event
	 * @return a mouse handler result
	 */
	static MouseHandlerResult resultOf(MouseEventx event, boolean consumed, View focus, View capture) {
		return new MouseHandlerResult(event, consumed, focus, capture);
	}

	/**
	 * Result from a {@link MouseHandler}.
	 *
	 * @param event the mouse event
	 * @param consumed flag telling if event was consumed
	 * @param focus the view which is requesting focus
	 * @param capture the view which captured an event
	 */
	record MouseHandlerResult(@Nullable MouseEventx event, boolean consumed, @Nullable View focus,
			@Nullable View capture) {
	}
}
