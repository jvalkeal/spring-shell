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
package org.springframework.shell.component.view.eventloop;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

class EventLoopTests {

	@Test
	void test1() {
		DefaultEventLoop loop = new DefaultEventLoop();

		Message<String> message = MessageBuilder.withPayload("TEST").build();

		StepVerifier verifier1 = StepVerifier.create(loop.events())
			.expectNextCount(1)
			.thenCancel()
			.verifyLater();

		StepVerifier verifier2 = StepVerifier.create(loop.events())
			.expectNextCount(1)
			.thenCancel()
			.verifyLater();

		loop.dispatch(message);
		verifier1.verify(Duration.ofSeconds(1));
		verifier2.verify(Duration.ofSeconds(1));
	}

	@Test
	void test2() {
		DefaultEventLoop loop = new DefaultEventLoop();
		Message<String> message = MessageBuilder.withPayload("TEST").build();

		StepVerifier verifier1 = StepVerifier.create(loop.events())
			.expectNextCount(1)
			.thenCancel()
			.verifyLater();

		loop.dispatch(message);
		verifier1.verify(Duration.ofSeconds(1));
	}

	@Test
	void test21() {
		DefaultEventLoop loop = new DefaultEventLoop();
		Message<String> message = MessageBuilder.withPayload("TEST").build();
		Flux<Message<String>> flux = Flux.just(message);

		StepVerifier verifier1 = StepVerifier.create(loop.events())
			.expectNextCount(1)
			.thenCancel()
			.verifyLater();

		loop.dispatch(flux);
		verifier1.verify(Duration.ofSeconds(1));
	}

	@Test
	void test22() {
		DefaultEventLoop loop = new DefaultEventLoop();
		Message<String> message = MessageBuilder.withPayload("TEST").build();
		Mono<Message<String>> mono = Mono.just(message);

		StepVerifier verifier1 = StepVerifier.create(loop.events())
			.expectNextCount(1)
			.thenCancel()
			.verifyLater();

		loop.dispatch(mono);
		verifier1.verify(Duration.ofSeconds(1));
	}

	@Test
	void test3() {
		DefaultEventLoop loop = new DefaultEventLoop();
		Message<String> message = MessageBuilder.withPayload("TEST").build();
		Mono<Message<String>> just = Mono.just(message);

		StepVerifier verifier1 = StepVerifier.create(loop.events())
			.expectNextCount(1)
			.thenCancel()
			.verifyLater();

		loop.subscribeTo(just);
		verifier1.verify(Duration.ofSeconds(1));
	}

	@Test
	void test4() {
		DefaultEventLoop loop = new DefaultEventLoop();

		StepVerifier verifier1 = StepVerifier.create(loop.events())
			.expectComplete()
			.verifyLater();

		loop.destroy();
		verifier1.verify(Duration.ofSeconds(1));
	}

}