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
 * Handles Key events in a form of {@link KeyHandlerArgs} and returns
 * {@link KeyHandlerResult}.
 *
 * @author Janne Valkealahti
 */
@FunctionalInterface
public interface KeyHandler {

	/**
	 * Handle Key event wrapped in a {@link KeyHandlerArgs}.
	 *
	 * @param args the Key handler arguments
	 * @return a handler result
	 */
	KeyHandlerResult handle(KeyHandlerArgs args);

    // default <V> Function<T, V> andThen(Function<? super R, ? extends V> after) {
    //     Objects.requireNonNull(after);
    //     return (T t) -> after.apply(apply(t));
    // }

    default KeyHandler either(KeyHandler handler) {
		if (handler != null) {
			return (KeyHandlerArgs args) -> handler.handle(args);
		}
		return (KeyHandlerArgs args) -> handle(args);
    }

    default KeyHandler eitherx(KeyHandler handler, Predicate<KeyHandlerResult> predicate) {
		return ddd -> {
			KeyHandlerResult res = handler.handle(ddd);
			if (predicate.test(res)) {
				return res;
			}
			return handle(ddd);
		};
    }

    default KeyHandler eitherxx(KeyHandler handler) {
		return eitherx(handler, res -> res.consumed());
    }

	/**
	 * Construct {@link KeyHandlerArgs} from a {@link KeyEvent}.
	 *
	 * @param event the Key event
	 * @return a Key handler args
	 */
	static KeyHandlerArgs argsOf(KeyEvent event) {
		return new KeyHandlerArgs(event);
	}

	/**
	 * Construct {@link KeyHandlerResult} from a {@link KeyEvent} and a
	 * {@link View}.
	 *
	 * @param event the Key event
	 * @param focus  the view
	 * @return a Key handler result
	 */
	static KeyHandlerResult resultOf(KeyEvent event, boolean consumed, View focus) {
		return new KeyHandlerResult(event, consumed, focus, null);
	}

	/**
	 * Arguments for a {@link KeyHandler}.
	 *
	 * @param event the Key event
	 */
	record KeyHandlerArgs(KeyEvent event) {
	}

	/**
	 * Result from a {@link KeyHandler}.
	 *
	 * @param event the Key event
	 * @param consumed flag telling if event was consumed
	 * @param focus the view which consumed an event
	 * @param capture the view which captured an event
	 */
	record KeyHandlerResult(@Nullable KeyEvent event, boolean consumed, @Nullable View focus,
			@Nullable View capture) {
	}
}
