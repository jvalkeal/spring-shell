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
package org.springframework.shell.samples.catalog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.component.TerminalUI;
import org.springframework.shell.component.view.AppView;
import org.springframework.shell.component.view.GridView;
import org.springframework.shell.component.view.ListView;
import org.springframework.shell.component.view.ListView.ListItem;
import org.springframework.shell.component.view.ListView.ListViewArgs;
import org.springframework.shell.component.view.StatusBarView;
import org.springframework.shell.component.view.StatusBarView.StatusItem;
import org.springframework.shell.component.view.event.EventLoop;
import org.springframework.shell.component.view.message.ShellMessageHeaderAccessor;
import org.springframework.shell.component.view.message.StaticShellMessageHeaderAccessor;
import org.springframework.shell.samples.catalog.scenario.Scenario;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.util.ObjectUtils;

/**
 * Main command access point to view showcase catalog.
 *
 * @author Janne Valkealahti
 */
@Command
public class CatalogCommand extends AbstractShellComponent {

	private final Logger log = LoggerFactory.getLogger(CatalogCommand.class);
	private final Map<String, List<Scenario>> scenarioMap = new TreeMap<>();

	public CatalogCommand(List<Scenario> scenarios) {
		scenarios.forEach(s -> {
			s.getCategories().forEach(category -> {
				List<Scenario> catScenarios = scenarioMap.computeIfAbsent(category, key -> new ArrayList<>());
				catScenarios.add(s);
			});
		});
	}

	// public static <T> Flux<T> xxx(EventLoop eventLoop, EventLoop.Type type, Class<T> clazz) {
	// 	return eventLoop.events()
	// 		.filter(m -> type.equals(StaticShellMessageHeaderAccessor.getEventType(m)))
	// 		.map(m -> m.getPayload())
	// 		.ofType(clazz)
	// 		.cast(clazz)
	// 		;
	// }

	@Command(command = "catalog")
	public void catalog(
	) {
		TerminalUI component = new TerminalUI(getTerminal());
		AppView app = new AppView();

		GridView grid = new GridView();
		grid.setRowSize(0, 1);
		grid.setColumnSize(30, 0);

		ListView scenarios = scenarios();

		ListView categories = categories();

		EventLoop eventLoop = component.getEventLoop();
		categories.setEventLoop(eventLoop);
		eventLoop.events(EventLoop.Type.VIEW, ListViewArgs.class)
			.doOnNext(args -> {
				log.info("CATEGORIES {}", args);
				List<ListItem> items = new ArrayList<>();
				items.add(new ListItem("111"));
				items.add(new ListItem("222"));
				scenarios.setItems(items);
			})
			.subscribe();

		// eventLoop.events()
		// 	.filter(m -> {
		// 		return ObjectUtils.nullSafeEquals(m.getHeaders().get(ShellMessageHeaderAccessor.EVENT_TYPE), EventLoop.Type.VIEW);
		// 	})
		// 	.doOnNext(m -> {
		// 		Object payload = m.getPayload();
		// 		if (payload instanceof ListViewArgs s) {
		// 			log.info("CATEGORIES {}", s);
		// 			List<ListItem> items = new ArrayList<>();
		// 			items.add(new ListItem("111"));
		// 			items.add(new ListItem("222"));
		// 			scenarios.setItems(items);
		// 		}
		// 	})
		// 	.subscribe();

		// categories.getMessageListeners().register(e -> {
		// 	log.info("CATEGORIES {}", e);
		// 	List<ListItem> items = new ArrayList<>();
		// 	items.add(new ListItem("111"));
		// 	items.add(new ListItem("222"));
		// 	scenarios.setItems(items);
		// });


		StatusBarView statusBar = statusBar();

		grid.addItem(categories, 0, 0, 1, 1, 0, 0);
		grid.addItem(scenarios, 0, 1, 1, 1, 0, 0);
		grid.addItem(statusBar, 1, 0, 1, 2, 0, 0);

		app.setMain(grid);

		component.setRoot(app, true);
		component.run();
	}

	private ListView categories() {
		ListView categories = new ListView();
		List<ListItem> items = new ArrayList<>();
		scenarioMap.keySet().forEach(c -> {
			items.add(new ListItem(c));
		});
		categories.setItems(items);

		categories.setTitle("Categories");
		categories.setShowBorder(true);

		// categories.getMessageListeners().register(e -> {
		// 	log.info("CATEGORIES {}", e);
		// });

		return categories;
	}

	private ListView scenarios() {
		ListView scenarios = new ListView();
		// List<ListItem> items = new ArrayList<>();
		// scenarios.forEach(s -> {
		// 	s.getCategories().forEach(c -> {
		// 		items.add(new ListItem(c));
		// 	});
		// });
		// categories.setItems(items);

		scenarios.setTitle("Scenarios");
		scenarios.setShowBorder(true);

		scenarios.getMessageListeners().register(e -> {
			log.info("SCENARIOS {}", e);
		});

		return scenarios;
	}

	private StatusBarView statusBar() {
		StatusBarView statusBar = new StatusBarView();
		StatusItem item1 = new StatusBarView.StatusItem("item1");
		StatusItem item2 = new StatusBarView.StatusItem("item2");
		statusBar.setItems(Arrays.asList(item1, item2));
		return statusBar;
	}

}
