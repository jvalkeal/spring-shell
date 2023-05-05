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


import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.component.view.BoxView;
import org.springframework.shell.component.view.ViewHandler;
import org.springframework.shell.standard.AbstractShellComponent;

/**
 * Commands showing some features view showcase.
 *
 * @author Janne Valkealahti
 */
@Command(command = { "view", "showcase" })
public class ViewShowcaseCommands extends AbstractShellComponent {

	@Command(command = "xxx1")
	public void xxx1() {
		ViewHandler component = new ViewHandler(getTerminal());
		BoxView box = new BoxView();
		box.setTitle("Title");
		box.setShowBorder(true);

		// Flux<Message<?>> asdf = Flux.interval(Duration.ofSeconds(1)).map(l -> {
		// 	Message<String> message = MessageBuilder
		// 		.withPayload("")
		// 		.setHeader("xxx", "tick")
		// 		.build();
		// 	return message;
		// });
		// component.getEventLoop().scheduleEvents2(asdf);

		// Flux<? extends Message<?>> events = component.getEventLoop().events();
		// events.doOnNext(m -> {
		// 	log.info("xxx1");
		// 	if (m.getHeaders().containsKey("xxx")) {
		// 		log.info("xxx2");
		// 		String t = new Date().toString();
		// 		box.setTitle(t);
		// 	}
		// })
		// .subscribe();

		// box.setInputConsumer(input -> {
		// 	log.info("xxx3 {}", input);
		// });

		component.setRoot(box, true);

		component.run();
	}

}
