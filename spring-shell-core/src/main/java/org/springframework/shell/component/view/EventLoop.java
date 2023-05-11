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

import reactor.core.publisher.Flux;

import org.springframework.messaging.Message;

/**
 *
 *
 * @author Janne Valkealahti
 */
public interface EventLoop {

	Flux<? extends Message<?>> events();
	Flux<KeyEvent> keyEvents();

	void subcribe(Flux<? extends Message<?>> messages);

	void dispatch(Flux<? extends Message<?>> messages);

	void dispatch(Message<?> message);

	// public final static String TYPE = "type";

	/**
	 * Type of an event.
	 */
	enum Type {

		/**
		 * Signals dispatched from a terminal.
		 */
		SIGNAL,

		/**
		 * Key bindings from a terminal.
		 */
		KEY,

		/**
		 * Mouse bindings from a terminal.
		 */
		MOUSE,

		/**
		 * System bindinds like redraw.
		 */
		SYSTEM
	}

	/**
	 * Contract to process event loop messages, possibly translating an event into
	 * some other type of event or events.
	 */
	interface EventLoopProcessor {

		/**
		 * Checks if this processor can process an event.
		 *
		 * @param message the message
		 * @return true if processor can process an event
		 */
		boolean canProcess(Message<?> message);

		/**
		 * Process a message and transform it into a new {@link Flux} of {@link Message}
		 * instances.
		 *
		 * @param message the message to process
		 * @return a flux of messages
		 */
		Flux<? extends Message<?>> process(Message<?> message);
	}
}
