/*
 * Copyright 2022 the original author or authors.
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
package org.springframework.shell.component;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.io.file.AccumulatorPathVisitor;
import org.apache.commons.io.file.Counters;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.InfoCmp.Capability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.shell.component.PathSearch.PathSearchContext;
import org.springframework.shell.component.PathSearch.PathSearchConfig.Mode;
import org.springframework.shell.component.PathSearch.PathSearchContext.PathViewItem;
import org.springframework.shell.component.context.ComponentContext;
import org.springframework.shell.component.support.AbstractTextComponent;
import org.springframework.shell.component.support.AbstractTextComponent.TextComponentContext;
import org.springframework.shell.component.support.AbstractTextComponent.TextComponentContext.MessageLevel;
import org.springframework.shell.support.search.SearchMatch;
import org.springframework.shell.support.search.SearchMatchResult;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import static org.jline.keymap.KeyMap.ctrl;
import static org.jline.keymap.KeyMap.key;

/**
 * Component resolving {@link Path} based on base path and optional search term.
 *
 * @author Janne Valkealahti
 */
public class PathSearch extends AbstractTextComponent<Path, PathSearchContext> {

	private final static Logger log = LoggerFactory.getLogger(PathSearch.class);
	private final PathSearchConfig config;
	private PathSearchContext currentContext;
	private Function<String, Path> pathProvider = (path) -> Paths.get(path);

	private List<Path> paths = new ArrayList<>();
	private List<PathViewItem> pathViews = new ArrayList<>();
	private AtomicInteger viewStart = new AtomicInteger(0);
	private AtomicInteger viewPosition = new AtomicInteger(0);

	public PathSearch(Terminal terminal) {
		this(terminal, null);
	}

	public PathSearch(Terminal terminal, String name) {
		this(terminal, name, null, null);
	}

	public PathSearch(Terminal terminal, String name, PathSearchConfig config) {
		this(terminal, name, null, config);
	}

	public PathSearch(Terminal terminal, String name, Function<PathSearchContext, List<AttributedString>> renderer) {
		this(terminal, name, renderer, null);
	}

	public PathSearch(Terminal terminal, String name, Function<PathSearchContext, List<AttributedString>> renderer, PathSearchConfig config) {
		super(terminal, name, null);
		setRenderer(renderer != null ? renderer : new DefaultRenderer());
		setTemplateLocation("classpath:org/springframework/shell/component/path-search-default.stg");
		this.config = config != null ? config : new PathSearchConfig();
	}

	@Override
	protected void bindKeyMap(KeyMap<String> keyMap) {
		super.bindKeyMap(keyMap);

		// additional binding what parent gives us
		keyMap.bind(OPERATION_DOWN, ctrl('E'), key(getTerminal(), Capability.key_down));
		keyMap.bind(OPERATION_UP, ctrl('Y'), key(getTerminal(), Capability.key_up));
	}

	@Override
	public PathSearchContext getThisContext(ComponentContext<?> context) {
		if (context != null && currentContext == context) {
			return currentContext;
		}
		currentContext = PathSearchContext.empty();
		currentContext.setName(getName());
		currentContext.setPathSearchConfig(this.config);
		context.stream().forEach(e -> {
			currentContext.put(e.getKey(), e.getValue());
		});
		return currentContext;
	}

	@Override
	protected boolean read(BindingReader bindingReader, KeyMap<String> keyMap, PathSearchContext context) {
		String operation = bindingReader.readBinding(keyMap);
		log.debug("Binding read result {}", operation);
		if (operation == null) {
			return true;
		}
		String input;
		switch (operation) {
			case OPERATION_CHAR:
				String lastBinding = bindingReader.getLastBinding();
				input = context.getInput();
				if (input == null) {
					input = lastBinding;
				}
				else {
					input = input + lastBinding;
				}
				context.setInput(input);
				checkPath(input, context);
				break;
			case OPERATION_BACKSPACE:
				input = context.getInput();
				if (StringUtils.hasLength(input)) {
					input = input.length() > 1 ? input.substring(0, input.length() - 1) : null;
				}
				context.setInput(input);
				checkPath(input, context);
				break;
			case OPERATION_EXIT:
				if (this.config.mode == Mode.DEFAULT) {
					if (StringUtils.hasText(context.getInput())) {
						context.setResultValue(Paths.get(context.getInput()));
					}
				}
				else if (this.config.mode == Mode.FUZZY) {
					context.setResultValue(this.paths.get(this.viewStart.get() + this.viewPosition.get()));
				}
				return true;
			case OPERATION_UP:
				if (viewStart.get() > 0 && viewPosition.get() == 0) {
					viewStart.decrementAndGet();
				}
				else if (viewStart.get() + viewPosition.get() >= pathViews.size()) {
					viewPosition.decrementAndGet();
				}
				else if (viewStart.get() + viewPosition.get() <= 0) {
					viewStart.set(this.paths.size() - this.config.getMaxPathsShow());
					viewPosition.set(pathViews.size() - 1);
				}
				else {
					viewPosition.decrementAndGet();
				}
				updatePathView(context);
				break;
			case OPERATION_DOWN:
				if (viewStart.get() + viewPosition.get() + 1 < this.config.getMaxPathsShow()) {
					viewPosition.incrementAndGet();
				}
				else if (viewStart.get() + viewPosition.get() + 1 >= this.paths.size()) {
					viewStart.set(0);
					viewPosition.set(0);
				}
				else {
					viewStart.incrementAndGet();
				}
				updatePathView(context);
				break;
			default:
				break;
		}
		return false;
	}

	/**
	 * Sets a path provider.
	 *
	 * @param pathProvider the path provider
	 */
	public void setPathProvider(Function<String, Path> pathProvider) {
		this.pathProvider = pathProvider;
	}

	/**
	 * Resolves a {@link Path} from a given raw {@code path}.
	 *
	 * @param path the raw path
	 * @return a resolved path
	 */
	protected Path resolvePath(String path) {
		return this.pathProvider.apply(path);
	}

	private void checkPath(String path, PathSearchContext context) {
		if (!StringUtils.hasText(path)) {
			context.setMessage(null);
			return;
		}
		Path p = resolvePath(path);
		boolean isDirectory = Files.isDirectory(p);
		if (isDirectory) {
			context.setMessage("Directory exists", MessageLevel.ERROR);
		}
		else {
			context.setMessage("Path ok", MessageLevel.INFO);
		}

		if (this.config.mode == Mode.FUZZY) {
			updatePaths(path, context);
			updatePathView(context);
		}
	}

	private void updatePaths(String path, PathSearchContext context) {
		this.paths = this.config.pathScanner.get().apply(path, context);
	}

	private void updatePathView(PathSearchContext context) {
		pathViews = buildPathView();
		context.setPathViewItems(pathViews);
	}

	private List<PathViewItem> buildPathView() {
		List<Path> view = this.paths.subList(this.viewStart.get(),
				Math.min(this.paths.size(), this.viewStart.get() + this.config.maxPathsShow));
		log.debug("Build path view, start {} items {}", this.viewStart.get(), view);
		List<PathViewItem> pathViews = IntStream.range(0, view.size())
			.mapToObj(i -> {
				PathViewItem item = new PathViewItem();
				item.name = view.get(i).toString();
				item.cursorRowRef = viewPosition;
				item.index = i;
				return item;
			})
			.collect(Collectors.toList());
		return pathViews;
	}

	/**
	 * Class defining configuration for path search.
	 */
	public static class PathSearchConfig {

		private Mode mode = Mode.DEFAULT;
		private int maxPathsShow = 5;
		private int maxPathsSearch = 20;
		private Supplier<BiFunction<String, PathSearchContext, List<Path>>> pathScanner = () -> DefaultPathScanner.of();

		public void setMode(Mode mode) {
			Assert.notNull(mode, "mode cannot be null");
			this.mode = mode;
		}

		public Mode getMode() {
			return this.mode;
		}

		public void setMaxPaths(int maxPathsShow, int maxPathsSearch) {
			Assert.state(maxPathsShow > 0 || maxPathsShow < 33, "maxPathsShow has to be between 1 and 32");
			Assert.state(maxPathsSearch > 0 || maxPathsSearch < 33, "maxPathsSearch has to be between 1 and 32");
			Assert.state(maxPathsSearch >= maxPathsShow, "maxPathsShow cannot be bigger than maxPathsSearch");
			this.maxPathsShow = maxPathsShow;
		}

		public int getMaxPathsShow() {
			return this.maxPathsShow;
		}

		public int getMaxPathsSearch() {
			return this.maxPathsSearch;
		}

		public void setPathScanner(Supplier<BiFunction<String, PathSearchContext, List<Path>>> pathScanner) {
			Assert.notNull(pathScanner, "pathScanner supplier cannot be null");
			this.pathScanner = pathScanner;
		}

		/**
		 * {@code PathSearch} has many modes it can operate on.
		 */
		public static enum Mode {

			/**
			 * Original operating mode where input checks if path exists and gives context
			 * indication of it.
			 */
			DEFAULT,

			/**
			 * Fuzzy selection mode searching files recursively and allows user to select
			 * path from a given single selector list.
			 */
			FUZZY;
		}

		@Override
		public String toString() {
			return "PathSearchConfig [maxPathsSearch=" + maxPathsSearch + ", maxPathsShow=" + maxPathsShow + ", mode="
					+ mode + "]";
		}
	}

	/**
	 * Context for {@link PathSearch}.
	 */
	public interface PathSearchContext extends TextComponentContext<Path, PathSearchContext> {

		/**
		 * Gets a path view items.
		 *
		 * @return path view items
		 */
		List<PathViewItem> getPathViewItems();

		/**
		 * Sets a path view items.
		 *
		 * @param items the path view items
		 */
		void setPathViewItems(List<PathViewItem> items);

		PathSearchConfig getPathSearchConfig();

		void setPathSearchConfig(PathSearchConfig config);

		/**
		 * Gets an empty {@link PathSearchContext}.
		 *
		 * @return empty path search context
		 */
		public static PathSearchContext empty() {
			return new DefaultPathSearchContext();
		}

		public static class PathViewItem {

			private String name;
			private Integer index;
			private AtomicInteger cursorRowRef;

			public String getName() {
				return name;
			}

			public Boolean getSelected() {
				return cursorRowRef.get() == index.intValue();
			}

		}
	}

	private static class DefaultPathSearchContext extends BaseTextComponentContext<Path, PathSearchContext>
			implements PathSearchContext {

		private List<PathViewItem> pathViewItems;
		private PathSearchConfig pathSearchConfig;

		@Override
		public List<PathViewItem> getPathViewItems() {
			return this.pathViewItems;
		}

		@Override
		public void setPathViewItems(List<PathViewItem> pathViewItems) {
			this.pathViewItems = pathViewItems;
		}

		@Override
		public PathSearchConfig getPathSearchConfig() {
			return this.pathSearchConfig;
		}

		@Override
		public void setPathSearchConfig(PathSearchConfig config) {
			this.pathSearchConfig = config;
		}

		@Override
		public Map<String, Object> toTemplateModel() {
			Map<String, Object> attributes = super.toTemplateModel();
			attributes.put("pathViewItems", getPathViewItems());
			Map<String, Object> model = new HashMap<>();
			model.put("model", attributes);
			return model;
		}
	}

	private class DefaultRenderer implements Function<PathSearchContext, List<AttributedString>> {

		@Override
		public List<AttributedString> apply(PathSearchContext context) {
			return renderTemplateResource(context.toTemplateModel());
		}
	}

	private static class ScoredPath implements Comparable<ScoredPath>{
		int score;
		Path path;
		ScoredPath(int score, Path path) {
			this.score = score;
			this.path = path;
		}
		static ScoredPath of(int score, Path path) {
			return new ScoredPath(score, path);
		}

		@Override
		public int compareTo(ScoredPath other) {
			return Integer.compare(other.score, this.score);
		}
	}

	private static class DefaultPathScanner implements BiFunction<String, PathSearchContext, List<Path>> {

		static DefaultPathScanner of() {
			return new DefaultPathScanner();
		}

		@Override
		public List<Path> apply(String input, PathSearchContext context) {

			PathSearchPathVisitor visitor = new PathSearchPathVisitor(context.getPathSearchConfig().getMaxPathsSearch());
			try {
				// Path path = StringUtils.hasText(input) ? Path.of(input) : Path.of("");
				Path path = Path.of("");
				log.debug("Walking input {} for path {}", input, path);
				Files.walkFileTree(path, visitor);
				log.debug("walked files {} dirs {}", visitor.getPathCounters().getFileCounter().get(),
						visitor.getPathCounters().getDirectoryCounter().get());
			} catch (Exception e) {
				log.debug("PathSearchPathVisitor caused exception", e);
			}


			TreeSet<ScoredPath> treeSet = new TreeSet<ScoredPath>();

			visitor.getFileList().forEach(p -> {
				SearchMatch searchMatch = SearchMatch.builder()
					.caseSensitive(false)
					.normalize(false)
					.forward(false)
					.build();
				SearchMatchResult result = searchMatch.match(p.toString(), input);
				treeSet.add(ScoredPath.of(result.getScore(), p));
			});
			return treeSet.stream()
				.map(sp -> sp.path)
				.limit(context.getPathSearchConfig().getMaxPathsSearch())
				.collect(Collectors.toList());
		}
	}

	/**
	 * Extension to AccumulatorPathVisitor which allows to break out from scanning
	 * when enough results are found.
	 */
	private static class PathSearchPathVisitor extends AccumulatorPathVisitor {

		private final int limitFiles;

		PathSearchPathVisitor(int limitFiles) {
			super(Counters.longPathCounters(), new WildcardFileFilter("*"), new WildcardFileFilter("*"));
			this.limitFiles = limitFiles;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
			FileVisitResult result = super.visitFile(file, attributes);
			if (getPathCounters().getFileCounter().get() >= this.limitFiles) {
				return FileVisitResult.TERMINATE;
			}
			return result;
		}
	}
}
