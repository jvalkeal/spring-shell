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

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Flow.Subscriber;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.Many;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.ObjectUtils;

/**
 * {@code EventLoop} is a central controller handling various events like
 * keyboard, mouse, terminal signals, redraw, etc.
 *
 * Main point of handling everything centrally is to support usual terminal
 * UI things like scheduled animation loops, redraw requests, etc, in order
 * while still supporting events with priorities.
 *
 * Event is always a spring messaging {@link Message}. Event payload
 * and headers depend on a message itself.
 *
 * @author Janne Valkealahti
 */
public class DefaultEventLoop implements EventLoop {

	private final static Logger log = LoggerFactory.getLogger(DefaultEventLoop.class);
	public final static String TYPE = "type";
	public final static String TYPE_SIGNAL = "signal";
	public final static String TYPE_TICK = "tick";
	private boolean running;
	private Many<Message<?>> bus;
	private Flux<? extends Message<?>> busFlux;
	private List<EventLoopProcessor> processors;

	/**
	 * Construct event loop with no procesors.
	 */
	public DefaultEventLoop(){
		this(null);
	}

	/**
	 * Construct event loop with a given processors.
	 */
	public DefaultEventLoop(List<EventLoopProcessor> processors) {
		this.processors = new ArrayList<>();
		if (processors != null) {
			this.processors.addAll(processors);
		}
		init();
	}

	private void init() {
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
	}

	/**
	 * Type of an event.
	 */
	public enum Type {

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
	public interface EventLoopProcessor {

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

	private static class TickProcessor implements EventLoopProcessor {

		@Override
		public boolean canProcess(Message<?> message) {
			return ObjectUtils.nullSafeEquals(message.getHeaders().get(DefaultEventLoop.TYPE), DefaultEventLoop.TYPE_TICK);
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

		// processors.add(new TickProcessor());

		this.toSchduleOnStart.forEach(f -> {
			f.subscribe();
		});

		running = true;
	}


	public void stop() {
		if (!running) {
			return;
		}
		running = false;
	}

	@Override
	public Flux<? extends Message<?>> events() {
		checkConditions();
		return busFlux;
	}

	@Override
	public void dispatch(Message<?> message) {
		checkConditions();
		bus.tryEmitNext(message);
	}

	public void dispatchTicks() {
		Message<String> message = MessageBuilder
			.withPayload("")
			.setHeader("type", "tick")
			.build();
		dispatch(message);
	}

	private List<Flux<? extends Message<?>>> toSchduleOnStart = new ArrayList<>();
	private List<Disposable> disposeOnStop = new ArrayList<>();

	public void scheduleEvents(Flux<Message<?>> messages) {
		Flux<Message<?>> f = messages
			.doOnNext(m -> {
				dispatch(m);
			});
		if (running) {
			Disposable subscribe = f.subscribe();
			disposeOnStop.add(subscribe);
			log.info("xxx asdf1");
		}
		else {
			this.toSchduleOnStart.add(f);
			log.info("xxx asdf2");
		}
	}

	@Override
	public void scheduleEventsx(Flux<? extends Message<?>> messages) {
		Flux<? extends Message<?>> f = messages
			.doOnNext(m -> {
				dispatch(m);
			});
		if (running) {
			Disposable subscribe = f.subscribe();
			disposeOnStop.add(subscribe);
			log.info("xxx asdf1");
		}
		else {
			this.toSchduleOnStart.add(f);
			log.info("xxx asdf2");
		}
	}

	@Override
	public void scheduleEventsAndSubcribe(Flux<? extends Message<?>> f) {
		// Disposable subscribe = messages
		// 	.doOnNext(m -> {
		// 		dispatch(m);
		// 	})
		// 	.subscribe()
		// 	;
		if (running) {
			Disposable subscribe = f.subscribe();
			disposeOnStop.add(subscribe);
			log.info("xxx asdf1");
		}
		else {
			this.toSchduleOnStart.add(f);
			log.info("xxx asdf2");
		}
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
