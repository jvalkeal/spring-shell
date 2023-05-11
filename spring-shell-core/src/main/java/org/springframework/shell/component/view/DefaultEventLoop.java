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
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.Many;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

public class DefaultEventLoop implements EventLoop {

	private final static Logger log = LoggerFactory.getLogger(DefaultEventLoop.class);
	private final Queue<Message<?>> messageQueue = new PriorityQueue<>(MessageComparator.comparingPriority());
	private final Many<Message<?>> sink = Sinks.many().unicast().onBackpressureBuffer(messageQueue);
	private final Flux<Message<?>> sinkFlux = sink.asFlux().share();
	private final Sinks.Many<Boolean> subscribedSignal = Sinks.many().replay().limit(1);
	private final Disposable.Composite upstreamSubscriptions = Disposables.composite();
	private final Scheduler scheduler = Schedulers.boundedElastic();
	private volatile boolean active = true;

	@Override
	public void dispatch(Message<?> message) {
		log.debug("dispatch {}", message);
		if (!doSend(message, 1000)) {
			log.warn("Failed to send message: {}", message);
		}
	}

	@Override
	public void subcribe(Flux<? extends Message<?>> messages) {
		upstreamSubscriptions.add(
		messages
			// .delaySubscription(subscribedSignal.asFlux().filter(Boolean::booleanValue).next())
			.subscribe()
		);
	}

	@Override
	public void dispatch(Flux<? extends Message<?>> messages) {
		subscribeTo(messages);
	}

	@Override
	public Flux<Message<?>> events() {
		return sinkFlux.share()
			.doFinally((s) -> subscribedSignal.tryEmitNext(sink.currentSubscriberCount() > 0))
		;
	}

	// Disposable subscribe2 = eventLoop.events()
	// .filter(m -> {
	// 	return ObjectUtils.nullSafeEquals(m.getHeaders().get(EventLoop.TYPE), EventLoop.Type.KEY);
	// })
	// .doOnNext(m -> {
	// 	Object payload = m.getPayload();
	// 	if (payload instanceof KeyEvent p) {
	// 		xxxk(p);
	// 	}
	// })
	// .subscribe();

	public Flux<KeyEvent> keyEvents() {
		return events()
			.filter(m -> EventLoop.Type.KEY.equals(StaticMessageHeaderAccessor.getEventType(m)))
			.map(m -> m.getPayload())
			.ofType(KeyEvent.class)
			.cast(KeyEvent.class);
	}


	private boolean doSend(Message<?> message, long timeout) {
		Assert.state(this.active && this.sink.currentSubscriberCount() > 0,
				() -> "The [" + this + "] doesn't have subscribers to accept messages");
		long remainingTime = 0;
		if (timeout > 0) {
			remainingTime = timeout;
		}
		long parkTimeout = 10;
		long parkTimeoutNs = TimeUnit.MILLISECONDS.toNanos(parkTimeout);
		while (this.active && !tryEmitMessage(message)) {
			remainingTime -= parkTimeout;
			if (timeout >= 0 && remainingTime <= 0) {
				return false;
			}
			LockSupport.parkNanos(parkTimeoutNs);
		}
		return true;
	}

	private boolean tryEmitMessage(Message<?> message) {
		return switch (sink.tryEmitNext(message)) {
			case OK -> true;
			case FAIL_NON_SERIALIZED, FAIL_OVERFLOW -> false;
			case FAIL_ZERO_SUBSCRIBER ->
					throw new IllegalStateException("The [" + this + "] doesn't have subscribers to accept messages");
			case FAIL_TERMINATED, FAIL_CANCELLED ->
					throw new IllegalStateException("Cannot emit messages into the cancelled or terminated sink: " + sink);
		};
	}

	public void subscribe(Subscriber<? super Message<?>> subscriber) {
		sink.asFlux()
			.doFinally((s) -> subscribedSignal.tryEmitNext(sink.currentSubscriberCount() > 0))
			.share()
			.subscribe(subscriber);

		upstreamSubscriptions.add(
			Mono.fromCallable(() -> sink.currentSubscriberCount() > 0)
					.filter(Boolean::booleanValue)
					.doOnNext(subscribedSignal::tryEmitNext)
					.repeatWhenEmpty((repeat) ->
							active ? repeat.delayElements(Duration.ofMillis(100)) : repeat)
					.subscribe());
	}

	public void subscribeTo(Publisher<? extends Message<?>> publisher) {
		upstreamSubscriptions.add(
			Flux.from(publisher)
				// .delaySubscription(subscribedSignal.asFlux().filter(Boolean::booleanValue).next())
				.publishOn(scheduler)
				.flatMap((message) ->
						Mono.just(message)
								.handle((messageToHandle, syncSink) -> sendReactiveMessage(messageToHandle))
								.contextWrite(StaticMessageHeaderAccessor.getReactorContext(message))
								)
				.contextCapture()
				.subscribe());
	}

	private void sendReactiveMessage(Message<?> message) {
		Message<?> messageToSend = message;
		// We have just restored Reactor context, so no need in a header anymore.
		if (messageToSend.getHeaders().containsKey(ShellMessageHeaderAccessor.REACTOR_CONTEXT)) {
			messageToSend =
					MessageBuilder.fromMessage(message)
							.removeHeader(ShellMessageHeaderAccessor.REACTOR_CONTEXT)
							.build();
		}
		try {
			dispatch(messageToSend);
			// if (!doSend(messageToSend, 1000)) {
			// 	log.warn("Failed to send message: {}", messageToSend);
			// }
		}
		catch (Exception ex) {
			log.warn("Error during processing event: {}", messageToSend);
		}
	}

	public void destroy() {
		this.active = false;
		this.upstreamSubscriptions.dispose();
		this.subscribedSignal.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
		this.sink.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
		this.scheduler.dispose();
	}

	private static class MessageComparator implements Comparator<Message<?>> {

		@Override
		public int compare(Message<?> left, Message<?> right) {
			Integer l = StaticMessageHeaderAccessor.getPriority(left);
			Integer r = StaticMessageHeaderAccessor.getPriority(right);
			if (l != null && r != null) {
				return l.compareTo(r);
			}
			return 0;
		}

		static Comparator<Message<?>> comparingPriority() {
			return new MessageComparator();
		}
	}

	// private static class TickProcessor implements EventLoopProcessor {

	// 	@Override
	// 	public boolean canProcess(Message<?> message) {
	// 		return ObjectUtils.nullSafeEquals(message.getHeaders().get(DefaultEventLoop.TYPE), DefaultEventLoop.TYPE_TICK);
	// 	}

	// 	@Override
	// 	public Flux<? extends Message<?>> process(Message<?> message) {
	// 		Flux<Message<Long>> take = Flux.interval(Duration.ofMillis(100))
	// 			.map(l -> {
	// 				return MessageBuilder
	// 					.withPayload(l)
	// 					.build();
	// 			})
	// 			.take(Duration.ofSeconds(1));
	// 		return take;
	// 	}

	// }

	// public DefaultEventLoop(List<EventLoopProcessor> processors) {
	// 	this.processors = new ArrayList<>();
	// 	if (processors != null) {
	// 		this.processors.addAll(processors);
	// 	}
	// 	init();
	// }

	// private void init() {
	// 	Queue<Message<?>> messageQueue = new PriorityQueue<>(MessageComparator.comparingPriority());
	// 	bus = Sinks.many().unicast().onBackpressureBuffer(messageQueue);
	// 	Flux<Message<?>> f1 = bus.asFlux();

	// 	Flux<? extends Message<?>> f2 = f1.flatMap(m -> {
	// 		Flux<? extends Message<?>> pm = null;
	// 		for (EventLoopProcessor processor : processors) {
	// 			if (processor.canProcess(m)) {
	// 				pm = processor.process(m);
	// 				break;
	// 			}
	// 		}
	// 		if (pm != null) {
	// 			return pm;
	// 		}
	// 		return Mono.just(m);
	// 	});
	// 	busFlux = f2.share();
	// }

}
