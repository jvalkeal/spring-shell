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
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.component.TerminalUI;
import org.springframework.shell.component.view.AppView;
import org.springframework.shell.component.view.GridView;
import org.springframework.shell.component.view.ListView;
import org.springframework.shell.component.view.ListView.ListViewArgs;
import org.springframework.shell.component.view.ListView.ListViewArgsx;
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



	@Command(command = "catalog")
	public void catalog(
	) {
		TerminalUI component = new TerminalUI(getTerminal());
		EventLoop eventLoop = component.getEventLoop();

		AppView app = new AppView();

		GridView grid = new GridView();
		grid.setRowSize(0, 1);
		grid.setColumnSize(30, 0);

		ListView<String> scenarios = scenarios();
		ListView<String> categories = categories(eventLoop);

		ParameterizedTypeReference<ListViewArgs<String>> typeRef = new ParameterizedTypeReference<ListViewArgs<String>>(){};
		eventLoop.events(EventLoop.Type.VIEW, typeRef)
			.filter(args -> args.view() == categories)
			.doOnNext(args -> {
				if (args.selected() != null) {
					String selected = args.selected();
					List<Scenario> list = scenarioMap.get(selected);
					List<String> collect = list.stream().map(s -> s.getTitle()).collect(Collectors.toList());
					scenarios.setItems(collect);
				}
			})
			.subscribe();

		// Disposable disposable = eventLoop.events(EventLoop.Type.VIEW, ListViewArgs.class)
		// 	.filter(args -> args.view() == categories)
		// 	.doOnNext(args -> {
		// 		log.info("CATEGORIES {}", args);
		// 		if (args.selected() != null) {
		// 			// String selected = args.selected();
		// 			// args.s
		// 		}
		// 		scenarios.setItems(Arrays.asList("111", "222"));
		// 	})
		// 	.subscribe();
		// eventLoop.onDestroy(disposable);

		StatusBarView statusBar = statusBar();

		grid.addItem(categories, 0, 0, 1, 1, 0, 0);
		grid.addItem(scenarios, 0, 1, 1, 1, 0, 0);
		grid.addItem(statusBar, 1, 0, 1, 2, 0, 0);

		app.setMain(grid);

		component.setRoot(app, true);
		component.run();
	}

	private ListView<String> categories(EventLoop eventLoop) {
		ListView<String> categories = new ListView<>();
		categories.setEventLoop(eventLoop);
		List<String> items = List.copyOf(scenarioMap.keySet());
		categories.setItems(items);

		categories.setTitle("Categories");
		categories.setShowBorder(true);

		return categories;
	}

	private ListView<String> scenarios() {
		ListView<String> scenarios = new ListView<>();
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
