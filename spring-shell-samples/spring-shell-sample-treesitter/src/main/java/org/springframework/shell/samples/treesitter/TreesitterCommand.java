/*
 * Copyright 2024 the original author or authors.
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
package org.springframework.shell.samples.treesitter;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.treesitter.TreeSitterLanguage;
import org.springframework.shell.treesitter.TreeSitterLanguageProvider;
import org.springframework.shell.treesitter.TreeSitterLanguages;
import org.springframework.shell.treesitter.TreeSitterNativeLoader;
import org.springframework.shell.treesitter.TreeSitterParser;
import org.springframework.shell.treesitter.TreeSitterPoint;
import org.springframework.shell.treesitter.TreeSitterQuery;
import org.springframework.shell.treesitter.TreeSitterQueryMatch;
import org.springframework.shell.treesitter.TreeSitterTree;
import org.springframework.util.FileCopyUtils;

/**
 * Main command access point to view showcase catalog.
 *
 * @author Janne Valkealahti
 */
@Command(command = "treesitter")
public class TreesitterCommand extends AbstractShellComponent {

	@Autowired
	TreeSitterLanguages treeSitterLanguages;

	@Command(command = "info", description = "Info about supported languages")
	public String info() {
		return treeSitterLanguages.getLanguages().stream().map(l -> l.getClass().toString())
				.collect(Collectors.joining(","));
	}

	@Command(command = "query", description = "Execute default highlight query for given file")
	public String query(
			@Option(required = true, defaultValue = "Path to a file, extension is the language id") File file
		) throws IOException {

		if (!file.exists()) {
			return String.format("File %s doesn't exist", file.getAbsolutePath());
		}

		String language = FilenameUtils.getExtension(file.getName());
		if (!treeSitterLanguages.getSupportedLanguages().contains(language)) {
			return String.format("Language with extension %s not supported", language);
		}

		// TreeSitterNativeLoader.initialize();
		// TreeSitterNativeLoader.initializeLanguage(language);
		// TreeSitterLanguageProvider<?> lp = treeSitterLanguages.getLanguage(language);
		// TreeSitterLanguage<?> language2 = lp.getLanguage();

		// TreeSitterQuery query = new TreeSitterQuery(language2, language2.highlightQuery());
		// TreeSitterParser parser = new TreeSitterParser();
		// parser.setLanguage(language2);
		byte[] bytes = FileCopyUtils.copyToByteArray(file);
		// TreeSitterTree tree = parser.parse(new String(bytes));
		// List<TreeSitterQueryMatch> matches = query.findMatches(tree.getRootNode());
		List<TreeSitterQueryMatch> matches = doMatch(language, bytes);
		StringBuilder builder = new StringBuilder();
		for (TreeSitterQueryMatch treeSitterQueryMatch : matches) {
			treeSitterQueryMatch.getCaptures().forEach(c -> {
				TreeSitterPoint startPoint = c.getNode().getStartPoint();
				TreeSitterPoint endPoint = c.getNode().getEndPoint();
				int startByte = c.getNode().getStartByte();
				int endByte = c.getNode().getEndByte();
				byte[] copyOfRange = Arrays.copyOfRange(bytes, startByte, endByte);

				builder.append(
						String.format("pattern: %s, capture: %s - [%s], start: (%s,%s), end: (%s,%s), text: `%s`",
								treeSitterQueryMatch.getPatternIndex(), treeSitterQueryMatch.getCaptureIndex(),
								treeSitterQueryMatch.getNames().stream().collect(Collectors.joining(", ")),
								startPoint.row(), startPoint.column(),
								endPoint.row(), endPoint.column(), new String(copyOfRange)));
				builder.append(System.lineSeparator());
			});
		}
		return builder.toString();
	}

	@Command(command = "highlight", description = "Syntax highlight a given file")
	public String highlight(
			@Option(required = true, defaultValue = "Path to a file, extension is the language id") File file
		) throws IOException {
		if (!file.exists()) {
			return String.format("File %s doesn't exist", file.getAbsolutePath());
		}

		String language = FilenameUtils.getExtension(file.getName());
		if (!treeSitterLanguages.getSupportedLanguages().contains(language)) {
			return String.format("Language with extension %s not supported", language);
		}

		byte[] bytes = FileCopyUtils.copyToByteArray(file);

		List<TreeSitterQueryMatch> matches = doMatch(language, bytes);

		for (TreeSitterQueryMatch treeSitterQueryMatch : matches) {
			treeSitterQueryMatch.getCaptures().forEach(c -> {
				int startByte = c.getNode().getStartByte();
				int endByte = c.getNode().getEndByte();

				// builder.append(
				// 		String.format("pattern: %s, capture: %s - [%s], start: (%s,%s), end: (%s,%s), text: `%s`",
				// 				treeSitterQueryMatch.getPatternIndex(), treeSitterQueryMatch.getCaptureIndex(),
				// 				treeSitterQueryMatch.getNames().stream().collect(Collectors.joining(", ")),
				// 				startPoint.row(), startPoint.column(),
				// 				endPoint.row(), endPoint.column(), new String(copyOfRange)));

			});
		}

		String out = new String(bytes);
		return out;
	}

	private List<TreeSitterQueryMatch> doMatch(String languageId, byte[] bytes) throws IOException {
		TreeSitterNativeLoader.initialize();
		TreeSitterNativeLoader.initializeLanguage(languageId);
		TreeSitterLanguageProvider<?> provider = treeSitterLanguages.getLanguage(languageId);
		TreeSitterLanguage<?> language = provider.getLanguage();
		TreeSitterQuery query = new TreeSitterQuery(language, language.highlightQuery());
		TreeSitterParser parser = new TreeSitterParser();
		parser.setLanguage(language);
		TreeSitterTree tree = parser.parse(new String(bytes));
		List<TreeSitterQueryMatch> matches = query.findMatches(tree.getRootNode());
		return matches;
	}
}
