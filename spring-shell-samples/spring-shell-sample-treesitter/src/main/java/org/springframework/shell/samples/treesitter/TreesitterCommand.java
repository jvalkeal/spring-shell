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
import org.springframework.shell.treesitter.TreeSitterLanguages;
import org.springframework.shell.treesitter.TreeSitterNativeLoader;
import org.springframework.shell.treesitter.TreeSitterParser;
import org.springframework.shell.treesitter.TreeSitterPoint;
import org.springframework.shell.treesitter.TreeSitterQuery;
import org.springframework.shell.treesitter.TreeSitterQueryMatch;
import org.springframework.shell.treesitter.TreeSitterTree;
import org.springframework.shell.treesitter.json.TreeSitterLanguageJson;
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

	@Command(command = "info")
	public String info() {
		return treeSitterLanguages.getLanguages().stream().map(l -> l.getClass().toString())
				.collect(Collectors.joining(","));
	}

	@Command(command = "query")
	public String query(@Option(required = true) File file) throws IOException {
		boolean exists = file.exists();
		String extension = FilenameUtils.getExtension(file.getName());

		TreeSitterNativeLoader.initialize();
		TreeSitterNativeLoader.initializeLanguage("json");
		TreeSitterLanguageJson json = new TreeSitterLanguageJson();
		TreeSitterQuery query = new TreeSitterQuery(json, TreeSitterLanguageJson.QUERY_HIGHLIGHT);
		TreeSitterParser parser = new TreeSitterParser();
		parser.setLanguage(json);
		byte[] bytes = FileCopyUtils.copyToByteArray(file);
		TreeSitterTree tree = parser.parse(new String(bytes));
		List<TreeSitterQueryMatch> matches = query.findMatches(tree.getRootNode());
		StringBuilder builder = new StringBuilder();
		for (TreeSitterQueryMatch treeSitterQueryMatch : matches) {
			treeSitterQueryMatch.getCaptures().forEach(c -> {
				TreeSitterPoint startPoint = c.getNode().getStartPoint();
				TreeSitterPoint endPoint = c.getNode().getEndPoint();
				int startByte = c.getNode().getStartByte();
				int endByte = c.getNode().getEndByte();
				byte[] copyOfRange = Arrays.copyOfRange(bytes, startByte, endByte);

				builder.append(String.format("pattern: %s, capture: %s, start: (%s,%s), end: (%s,%s), text: `%s`",
						treeSitterQueryMatch.getPatternIndex(), treeSitterQueryMatch.getCaptureIndex(), startPoint.row(), startPoint.column(),
						endPoint.row(), endPoint.column(), new String(copyOfRange)));
				builder.append(System.lineSeparator());
			});
		}
		return builder.toString();
		// return String.format("ext=%s, exists=%s", extension, exists);
	}
}
