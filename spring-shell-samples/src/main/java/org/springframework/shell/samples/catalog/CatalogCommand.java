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

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.messaging.Message;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.component.TerminalUI;
import org.springframework.shell.component.view.AppView;
import org.springframework.shell.component.view.GridView;
import org.springframework.shell.component.view.ListView;
import org.springframework.shell.component.view.ListView.ListViewAction;
import org.springframework.shell.component.view.StatusBarView;
import org.springframework.shell.component.view.StatusBarView.StatusItem;
import org.springframework.shell.component.view.control.cell.ListCell;
import org.springframework.shell.component.view.View;
import org.springframework.shell.component.view.event.EventLoop;
import org.springframework.shell.component.view.event.KeyEvent.ModType;
import org.springframework.shell.component.view.geom.Rectangle;
import org.springframework.shell.component.view.message.ShellMessageBuilder;
import org.springframework.shell.component.view.screen.Screen;
import org.springframework.shell.component.view.screen.Screen.Writer;
import org.springframework.shell.samples.catalog.scenario.Scenario;
import org.springframework.shell.samples.catalog.scenario.ScenarioComponent;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Main command access point to view showcase catalog.
 *
 * @author Janne Valkealahti
 */
@Command
public class CatalogCommand extends AbstractShellComponent {

	private final static Logger log = LoggerFactory.getLogger(CatalogCommand.class);
	private final static ParameterizedTypeReference<ListViewAction<String>> LISTVIEW_STRING_TYPEREF
		= new ParameterizedTypeReference<ListViewAction<String>>() {};
	private final static ParameterizedTypeReference<ListViewAction<ScenarioData>> LISTVIEW_SCENARIO_TYPEREF
		= new ParameterizedTypeReference<ListViewAction<ScenarioData>>() {};

	// mapping from category name to scenarios(can belong to multiple categories)
	private final Map<String, List<ScenarioData>> categoryMap = new TreeMap<>();
	private View currentScenarioView = null;

	public CatalogCommand(List<Scenario> scenarios) {
		mapScenarios(scenarios);
	}

	@Command(command = "catalog")
	public void catalog(
	) {
		TerminalUI component = new TerminalUI(getTerminal());
		EventLoop eventLoop = component.getEventLoop();
		AppView app = scenarioBrowser(eventLoop, component);

		eventLoop.onDestroy(eventLoop.keyEvents()
			.doOnNext(m -> {
				if ("q".equals(m.data()) && m.mod().contains(ModType.CTRL)) {
					if (currentScenarioView != null) {
						currentScenarioView = null;
						component.setRoot(app, true);
					}
					else {
						Message<String> msg = ShellMessageBuilder.withPayload("int")
							.setEventType(EventLoop.Type.SYSTEM)
							.setPriority(0)
							.build();
						eventLoop.dispatch(msg);
					}
				}
			})
			.subscribe());

		component.setRoot(app, true);
		component.run();
	}

	private void mapScenarios(List<Scenario> scenarios) {
		// we blindly expect scenario to have ScenarioComponent annotation with all fields
		scenarios.forEach(sce -> {
			ScenarioComponent ann = AnnotationUtils.findAnnotation(sce.getClass(), ScenarioComponent.class);
			if (ann != null) {
				String name = ann.name();
				String description = ann.description();
				String[] category = ann.category();
				if (StringUtils.hasText(name) && StringUtils.hasText(description) && !ObjectUtils.isEmpty(category)) {
					for (String cat : category) {
						ScenarioData scenarioData = new ScenarioData(sce, name, description, category);
						categoryMap.computeIfAbsent(Scenario.CATEGORY_ALL, key -> new ArrayList<>()).add(scenarioData);
						categoryMap.computeIfAbsent(cat, key -> new ArrayList<>()).add(scenarioData);
					}
				}
			}
		});
	}

	private AppView scenarioBrowser(EventLoop eventLoop, TerminalUI component) {
		AppView app = new AppView();

		GridView grid = new GridView();
		grid.setRowSize(0, 1);
		grid.setColumnSize(30, 0);

		ListView<String> categories = categorySelector(eventLoop);
		ListView<ScenarioData> scenarios = scenarioSelector(eventLoop);

		eventLoop.onDestroy(eventLoop.events(EventLoop.Type.VIEW, LISTVIEW_SCENARIO_TYPEREF)
			.filter(args -> args.view() == scenarios)
			.doOnNext(args -> {
				if (args.item() != null) {
					switch (args.action()) {
						case "OpenSelectedItem":
							View view = args.item().scenario().configure(eventLoop).build();
							component.setRoot(view, true);
							currentScenarioView = view;
							break;
						default:
							break;
					}
				}
			})
			.subscribe());

		eventLoop.onDestroy(eventLoop.events(EventLoop.Type.VIEW, LISTVIEW_STRING_TYPEREF)
			.filter(args -> args.view() == categories)
			.doOnNext(args -> {
				if (args.item() != null) {
					switch (args.action()) {
						case "LineUp":
						case "LineDown":
							String selected = args.item();
							List<ScenarioData> list = categoryMap.get(selected);
							scenarios.setItems(list);
							break;
						default:
							break;
					}
				}
			})
			.subscribe());

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
		List<String> items = List.copyOf(categoryMap.keySet());
		categories.setItems(items);
		categories.setTitle("Categories");
		categories.setShowBorder(true);
		return categories;
	}

	private ListView<ScenarioData> scenarioSelector(EventLoop eventLoop) {
		ListView<ScenarioData> scenarios = new ListView<>();
		scenarios.setEventLoop(eventLoop);
		scenarios.setTitle("Scenarios");
		scenarios.setShowBorder(true);
		scenarios.setCellFactory(list -> new ScenarioListCell());
		return scenarios;
	}

	private StatusBarView statusBar() {
		StatusBarView statusBar = new StatusBarView();
		StatusItem item1 = new StatusBarView.StatusItem("item1");
		StatusItem item2 = new StatusBarView.StatusItem("item2");
		statusBar.setItems(Arrays.asList(item1, item2));
		return statusBar;
	}

	private record ScenarioData(Scenario scenario, String name, String description, String[] category){};

	private static class ScenarioListCell extends ListCell<ScenarioData> {

		@Override
		public void draw(Screen screen) {
			Rectangle rect = getRect();
			Writer writer = screen.writerBuilder().build();
			writer.text(String.format("%s %s", getItem().name(), getItem().description()), rect.x(), rect.y());
			writer.background(rect, getBackgroundColor());
		}
	}

}
