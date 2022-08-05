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
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.InfoCmp.Capability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.shell.component.PathInput.PathInputContext;
import org.springframework.shell.component.context.ComponentContext;
import org.springframework.shell.component.support.AbstractTextComponent;
import org.springframework.shell.component.support.AbstractTextComponent.TextComponentContext;
import org.springframework.shell.component.support.AbstractTextComponent.TextComponentContext.MessageLevel;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import static org.jline.keymap.KeyMap.ctrl;
import static org.jline.keymap.KeyMap.key;

/**
 * Component resolving {@link Path} based on input from an user.
 *
 * In a shell {@link Path} needs to be resolved based on some user input. In
 * its simplest form user types a path and that is returned by giving some
 * contextual info about path itself. For example user can input path which
 * exists and contextual info can notify about that but it's outside of how
 * path would get used. We're just trying to make it easier for providing
 * enough info so that user can pick path useable in a running shell app.
 *
 * While every single use case could be implemented as its own class this
 * implementation is trying to cover most of a use cases via configuration
 * how component works.
 *
 * @author Janne Valkealahti
 */
public class PathInput extends AbstractTextComponent<Path, PathInputContext> {

	private final static Logger log = LoggerFactory.getLogger(PathInput.class);
	private PathInputContext currentContext;
	private Function<String, Path> pathProvider = (path) -> Paths.get(path);

	public PathInput(Terminal terminal) {
		this(terminal, null);
	}

	public PathInput(Terminal terminal, String name) {
		this(terminal, name, null);
	}

	public PathInput(Terminal terminal, String name, Function<PathInputContext, List<AttributedString>> renderer) {
		super(terminal, name, null);
		setRenderer(renderer != null ? renderer : new DefaultRenderer());
		setTemplateLocation("classpath:org/springframework/shell/component/path-input-default.stg");
	}

	@Override
	protected void bindKeyMap(KeyMap<String> keyMap) {
		super.bindKeyMap(keyMap);

		// additional binding what parent gives us
		keyMap.bind(OPERATION_DOWN, ctrl('E'), key(getTerminal(), Capability.key_down));
		keyMap.bind(OPERATION_UP, ctrl('Y'), key(getTerminal(), Capability.key_up));
	}

	@Override
	public PathInputContext getThisContext(ComponentContext<?> context) {
		if (context != null && currentContext == context) {
			return currentContext;
		}
		currentContext = PathInputContext.empty();
		currentContext.setName(getName());
		context.stream().forEach(e -> {
			currentContext.put(e.getKey(), e.getValue());
		});
		return currentContext;
	}

	@Override
	protected boolean read(BindingReader bindingReader, KeyMap<String> keyMap, PathInputContext context) {
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
				if (StringUtils.hasText(context.getInput())) {
					context.setResultValue(Paths.get(context.getInput()));
				}
				return true;
			case OPERATION_UP:
				break;
			case OPERATION_DOWN:
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

	private void checkPath(String path, PathInputContext context) {
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

		scanPath(path, context);
	}

	/**
	 * Internal scan method which fires up a scan and provides update to a
	 * context what comes for a scanned paths.
	 */
	private void scanPath(String path, PathInputContext context) {
		DefaultPathScanner scanner = new DefaultPathScanner();
		List<Path> apply = scanner.apply(path, context);
		log.info("XXX1 {}", apply);
		context.setPaths(apply.stream().map(Path::toString).collect(Collectors.toList()));
	}

	/**
	 * Class defining configuration for path input.
	 */
	public static class PathInputConfig {

		private Mode mode = Mode.DEFAULT;
		private boolean skipFiles = false;
		private boolean skipDirs = false;
		private int maxItems = 5;
		private BiFunction<String, PathInputContext, List<Path>> pathScanner;

		public void setMaxItems(int maxItems) {
			Assert.state(maxItems > 0 || maxItems < 33, "maxItems has to be between 1 and 32");
			this.maxItems = maxItems;
		}

		/**
		 * {@code PathInput} has many modes it can operate on.
		 */
		public static enum Mode {
			DEFAULT,
			FUZZY;
		}
	}

	private static class DefaultPathScanner implements BiFunction<String, PathInputContext, List<Path>> {

		@Override
		public List<Path> apply(String input, PathInputContext context) {
			PathGatheringFileVisitor fv = new PathGatheringFileVisitor();
			try {
				if (StringUtils.hasText(input)) {
					Path xxx = Path.of(input);
					if (Files.exists(xxx)) {
						Files.walkFileTree(Path.of(input), fv);
					}
				}
			} catch (IOException e) {
				log.error("XXX2", e);
			}
			return fv.getMatches();
		}
	}

	/**
	 * Context for {@link PathInput}.
	 */
	public interface PathInputContext extends TextComponentContext<Path, PathInputContext> {

		/**
		 * Gets a scanned paths.
		 *
		 * @return scanned paths
		 */
		List<String> getPaths();

		/**
		 * Sets a scanned paths.
		 *
		 * @param paths the scanned paths
		 */
		void setPaths(List<String> paths);

		/**
		 * Gets an empty {@link PathInputContext}.
		 *
		 * @return empty path input context
		 */
		public static PathInputContext empty() {
			return new DefaultPathInputContext();
		}
	}

	private static class DefaultPathInputContext extends BaseTextComponentContext<Path, PathInputContext>
			implements PathInputContext {

		private List<String> paths;

		@Override
		public List<String> getPaths() {
			return paths;
		}

		@Override
		public void setPaths(List<String> paths) {
			this.paths = paths;
		}

		@Override
		public Map<String, Object> toTemplateModel() {
			Map<String, Object> attributes = super.toTemplateModel();
			attributes.put("paths", getPaths());
			Map<String, Object> model = new HashMap<>();
			model.put("model", attributes);
			return model;
		}
	}

	private class DefaultRenderer implements Function<PathInputContext, List<AttributedString>> {

		@Override
		public List<AttributedString> apply(PathInputContext context) {
			return renderTemplateResource(context.toTemplateModel());
		}
	}

	/**
	 * Implementation of a {@link FileVisitor} used to scan given paths by
	 * using patterns for path resolving.
	 */
	private static class PathGatheringFileVisitor extends SimpleFileVisitor<Path> {

		protected final List<Path> matches = new ArrayList<>();
		private final Set<Pattern> forbiddenDirectoryPatterns;
		private final Set<Pattern> forbiddenFilenamePatterns;

		public PathGatheringFileVisitor() {
			this(Collections.singleton("\\Q.git\\E"), Collections.singleton(".*~"));
		}

		public PathGatheringFileVisitor(Set<String> forbiddenDirectoryPatterns, Set<String> forbiddenFilenamePatterns) {
			this.forbiddenDirectoryPatterns = forbiddenDirectoryPatterns.stream().map(Pattern::compile)
					.collect(Collectors.toSet());
			this.forbiddenFilenamePatterns = forbiddenFilenamePatterns.stream().map(Pattern::compile)
					.collect(Collectors.toSet());
		}

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
			if (dir.getFileName() == null) {
				return FileVisitResult.TERMINATE;
			}
			String dirName = dir.getFileName().toString();
			return forbiddenDirectoryPatterns.stream().noneMatch((p) -> p.matcher(dirName).matches())
					? FileVisitResult.CONTINUE : FileVisitResult.SKIP_SUBTREE;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			if (!rejectFile(file, attrs)) {
				matches.add(file);
			}
			return super.visitFile(file, attrs);
		}

		public List<Path> getMatches() {
			return matches;
		}

		protected boolean rejectFile(Path file, BasicFileAttributes attrs) {
			String filename = file.getFileName().toString();
			return forbiddenFilenamePatterns.stream().anyMatch((p) -> p.matcher(filename).matches());
		}
	}
}
