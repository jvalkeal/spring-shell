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

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.shell.component.view.event.EventLoop.EventLoopProcessor;
import org.springframework.shell.component.view.message.StaticShellMessageHeaderAccessor;

public class TaskEventLoopProcessor implements EventLoopProcessor {

	@Override
	public boolean canProcess(Message<?> message) {
		if (EventLoop.Type.TASK.equals(StaticShellMessageHeaderAccessor.getEventType(message))) {
			Object payload = message.getPayload();
			if (payload instanceof Runnable r) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Flux<? extends Message<?>> process(Message<?> message) {
		return Mono.just(message.getPayload())
			.ofType(Runnable.class)
			.flatMap(Mono::fromRunnable)
			.then(Mono.just(MessageBuilder.withPayload(new Object()).build()))
			.flux();
	}
}
