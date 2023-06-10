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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.messaging.Message;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.component.TerminalUI;
import org.springframework.shell.component.view.AppView;
import org.springframework.shell.component.view.GridView;
import org.springframework.shell.component.view.ListView;
import org.springframework.shell.component.view.ListView.ListViewAction;
import org.springframework.shell.component.view.StatusBarView;
import org.springframework.shell.component.view.StatusBarView.StatusItem;
import org.springframework.shell.component.view.View;
import org.springframework.shell.component.view.event.EventLoop;
import org.springframework.shell.component.view.event.KeyEvent.ModType;
import org.springframework.shell.component.view.message.ShellMessageBuilder;
import org.springframework.shell.samples.catalog.scenario.Scenario;
import org.springframework.shell.standard.AbstractShellComponent;

/**
 * Main command access point to view showcase catalog.
 *
 * @author Janne Valkealahti
 */
@Command
public class CatalogCommand extends AbstractShellComponent {

	private final Logger log = LoggerFactory.getLogger(CatalogCommand.class);
	// mapping from category name to scenarios(can belong to multiple categories)
	private final Map<String, List<Scenario>> scenarioMap = new TreeMap<>();
	private final Map<String, Scenario> scenarioNameMap = new HashMap<>();

	public CatalogCommand(List<Scenario> scenarios) {
		scenarios.forEach(sce -> {
			scenarioNameMap.put(sce.getTitle(), sce);
			sce.getCategories().forEach(cat -> {
				scenarioMap.computeIfAbsent(cat, key -> new ArrayList<>()).add(sce);
			});
		});
	}

	@Command(command = "catalog")
	public void catalog(
	) {
		TerminalUI component = new TerminalUI(getTerminal());
		EventLoop eventLoop = component.getEventLoop();

		eventLoop.keyEvents()
			.doOnNext(m -> {
				if ("q".equals(m.data()) && m.mod().contains(ModType.CTRL)) {
					Message<String> msg = ShellMessageBuilder.withPayload("int")
						.setEventType(EventLoop.Type.SYSTEM)
						.setPriority(0)
						.build();
					eventLoop.dispatch(msg);
				}
				else if ("w".equals(m.data()) && m.mod().contains(ModType.CTRL)) {
					// component.setRoot(box, true);
				}
			})
			.subscribe();


		AppView app = scenarioBrowser(eventLoop, component);

		component.setRoot(app, true);
		component.run();
	}

	private final static ParameterizedTypeReference<ListViewAction<String>> LISTVIEW_STRING_TYPEREF = new ParameterizedTypeReference<ListViewAction<String>>() {
	};

	private AppView scenarioBrowser(EventLoop eventLoop, TerminalUI component) {
		AppView app = new AppView();

		GridView grid = new GridView();
		grid.setRowSize(0, 1);
		grid.setColumnSize(30, 0);

		ListView<String> scenarios = scenarioSelector(eventLoop);
		ListView<String> categories = categorySelector(eventLoop);

		Disposable disposable1 = eventLoop.events(EventLoop.Type.VIEW, LISTVIEW_STRING_TYPEREF)
			.filter(args -> args.view() == scenarios)
			.doOnNext(args -> {
				if (args.item() != null) {
					switch (args.action()) {
						case "OpenSelectedItem":
							String title = args.item();
							Scenario s = scenarioNameMap.get(title);
							s.configure(eventLoop);
							View v = s.getView();
							component.setRoot(v, true);
							break;
						default:
							break;
					}
				}
			})
			.subscribe();
		eventLoop.onDestroy(disposable1);

		Disposable disposable2 = eventLoop.events(EventLoop.Type.VIEW, LISTVIEW_STRING_TYPEREF)
			.filter(args -> args.view() == categories)
			.doOnNext(args -> {
				if (args.item() != null) {
					switch (args.action()) {
						case "LineUp":
						case "LineDown":
							String selected = args.item();
							List<Scenario> list = scenarioMap.get(selected);
							List<String> collect = list.stream().map(sce -> sce.getTitle()).collect(Collectors.toList());
							scenarios.setItems(collect);
							break;
						default:
							break;
					}
				}
			})
			.subscribe();
		eventLoop.onDestroy(disposable2);


		StatusBarView statusBar = statusBar();

		grid.addItem(categories, 0, 0, 1, 1, 0, 0);
		grid.addItem(scenarios, 0, 1, 1, 1, 0, 0);
		grid.addItem(statusBar, 1, 0, 1, 2, 0, 0);

		app.setMain(grid);

		return app;
	}

	private ListView<String> categorySelector(EventLoop eventLoop) {
		ListView<String> categories = new ListView<>();
		categories.setEventLoop(eventLoop);
		List<String> items = List.copyOf(scenarioMap.keySet());
		categories.setItems(items);

		categories.setTitle("Categories");
		categories.setShowBorder(true);

		return categories;
	}

	private ListView<String> scenarioSelector(EventLoop eventLoop) {
		ListView<String> scenarios = new ListView<>();
		scenarios.setEventLoop(eventLoop);
		scenarios.setTitle("Scenarios");
		scenarios.setShowBorder(true);

		// scenarios.getMessageListeners().register(e -> {
		// 	log.info("SCENARIOS {}", e);
		// });

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
