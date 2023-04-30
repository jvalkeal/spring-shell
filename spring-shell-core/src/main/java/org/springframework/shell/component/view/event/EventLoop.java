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

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.function.Function;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.Many;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.ObjectUtils;

/**
 *
 *
 * @author Janne Valkealahti
 */
public class EventLoop {

	public final static String TYPE = "type";
	public final static String TYPE_SIGNAL = "signal";
	public final static String TYPE_TICK = "tick";
	// public final static String TYPE_RERDRAW = "redraw";

	private boolean running;
	private Many<Message<?>> bus;
	private Flux<? extends Message<?>> busFlux;

	private List<EventLoopProcessor> processors = new ArrayList<>();

	public enum Type {
		SIGNAL
	}

	public EventLoop() {
	}

	interface EventLoopProcessor {
		boolean canProcess(Message<?> message);
		Flux<? extends Message<?>> process(Message<?> message);
	}

	private static class TickProcessor implements EventLoopProcessor {

		@Override
		public boolean canProcess(Message<?> message) {
			return ObjectUtils.nullSafeEquals(message.getHeaders().get(EventLoop.TYPE), EventLoop.TYPE_TICK);
		}

		@Override
		public Flux<? extends Message<?>> process(Message<?> message) {
			Flux<Message<Long>> take = Flux.interval(Duration.ofMillis(100))
				.map(l -> {
					return MessageBuilder
						.withPayload(l)
						.build();
				})
				.take(Duration.ofSeconds(1));
			return take;
		}

	}

	public void start() {
		if (running) {
			return;
		}

		processors.add(new TickProcessor());

		Queue<Message<?>> messageQueue = new PriorityQueue<>(MessageComparator.comparingPriority());
		bus = Sinks.many().unicast().onBackpressureBuffer(messageQueue);
		Flux<Message<?>> f1 = bus.asFlux();

		Flux<? extends Message<?>> flatMap = f1.flatMap(m -> {
			Flux<? extends Message<?>> xxx = null;
			for (EventLoopProcessor processor : processors) {
				if (processor.canProcess(m)) {
					xxx = processor.process(m);
					break;
				}
			}
			if (xxx != null) {
				return xxx;
			}
			return Mono.just(m);
		});
		busFlux = flatMap.share();

		running = true;
	}


	public void stop() {
		if (!running) {
			return;
		}
		running = false;
	}

	public Flux<? extends Message<?>> events() {
		checkConditions();
		return busFlux;
	}

	public void dispatch(Message<?> message) {
		checkConditions();
		bus.tryEmitNext(message);
	}

	public void dispatchTicks() {
		Message<String> message = MessageBuilder
			.withPayload("WINCH")
			.setHeader("type", "tick")
			.build();
		dispatch(message);
	}

	private void checkConditions() {
		if (bus == null) {
			throw new IllegalStateException("EventLoop is not running");
		}
	}

	private static class MessageComparator implements Comparator<Message<?>> {

		@Override
		public int compare(Message<?> left, Message<?> right) {
			return 0;
		}

		static Comparator<Message<?>> comparingPriority() {
			return new MessageComparator();
		}
	}
}
