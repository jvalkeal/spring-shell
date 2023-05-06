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
package org.springframework.shell.samples.view;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import reactor.core.publisher.Flux;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.component.view.BoxView;
import org.springframework.shell.component.view.EventLoop;
import org.springframework.shell.component.view.ViewHandler;
import org.springframework.shell.standard.AbstractShellComponent;

/**
 * Commands showing some features view showcase.
 *
 * @author Janne Valkealahti
 */
@Command(command = { "view", "showcase" })
public class ViewShowcaseCommands extends AbstractShellComponent {

	@Command(command = "clock")
	public void clock() {
		// setup handler with one box view
		ViewHandler component = new ViewHandler(getTerminal());
		BoxView root = new BoxView();
		root.setTitle("What's o'clock");
		root.setShowBorder(true);

		// hack to store date to print
		AtomicReference<String> ref = new AtomicReference<>();

		// date message as flux
		Flux<Message<?>> dates = Flux.interval(Duration.ofSeconds(1)).map(l -> {
			String date = new Date().toString();
			Message<String> message = MessageBuilder
				.withPayload(date)
				.setHeader("xxx", "tick")
				.build();
			return message;
		});

		// schedule dates to event loop
		component.getEventLoop().scheduleEventsx(dates);

		// process dates
		Flux<? extends Message<?>> datesProcessing = component.getEventLoop().events()
			.filter(message -> message.getHeaders().containsKey("xxx"))
			.doOnNext(message -> {
				if(message.getPayload() instanceof String s) {
					ref.set(s);
					Message<String> xxx = MessageBuilder.withPayload("WINCH")
						.setHeader(EventLoop.TYPE, EventLoop.Type.SIGNAL)
						.build();
					component.getEventLoop().dispatch(xxx);
				}
			});
		// schedule detas processing
		component.getEventLoop().scheduleEventsAndSubcribe(datesProcessing);

		// draw current date
		root.setDrawFunction((screen, rect) -> {
			String s = ref.get();
			if (s != null) {
				screen.print(s, 2, 2, s.length());
			}
			return rect;
		});

		// logic run
		component.setRoot(root, true);
		component.run();
	}
}
