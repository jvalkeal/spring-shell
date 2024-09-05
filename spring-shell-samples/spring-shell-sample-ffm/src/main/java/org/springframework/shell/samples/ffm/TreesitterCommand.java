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
package org.springframework.shell.samples.ffm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.treesitter.TreeSitterLanguages;
import org.springframework.shell.treesitter.TreeSitterPoint;
import org.springframework.shell.treesitter.TreeSitterQueryMatch;
import org.springframework.util.FileCopyUtils;

/**
 * Main command access point to view showcase catalog.
 *
 * @author Janne Valkealahti
 */
@Command(command = "treesitter")
public class TreesitterCommand extends AbstractShellComponent {

	private final static Logger log = LoggerFactory.getLogger(TreesitterCommand.class);

	@Autowired
	TreeSitterLanguages treeSitterLanguages;

	@Command(command = "info", description = "Info about supported languages")
	public String info() {
		return treeSitterLanguages.getLanguageProviders().stream().map(l -> l.getClass().toString())
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

		byte[] bytes = FileCopyUtils.copyToByteArray(file);
		List<TreeSitterQueryMatch> matches = treeSitterLanguages.languageMatch(language, bytes);
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

		List<TreeSitterQueryMatch> matches = treeSitterLanguages.languageMatch(language, bytes);

		List<HighlightData> highlights = new ArrayList<>();
		int hIndex = -1;

		for (TreeSitterQueryMatch treeSitterQueryMatch : matches) {
			treeSitterQueryMatch.getCaptures().forEach(c -> {
				int startByte = c.getNode().getStartByte();
				int endByte = c.getNode().getEndByte();
				if (endByte > hIndex) {
					highlights.add(new HighlightData(treeSitterQueryMatch.getNames().getLast(), startByte, endByte));
				}

			});
		}

		StringBuilder buf = new StringBuilder();
		int ti = 0;

		for (HighlightData data : highlights) {
			int startByte = data.start();
			int endByte = data.end();
			String hKey = data.key();
			if (startByte >= ti) {
				byte[] x1 = new byte[startByte - ti];
				System.arraycopy(bytes, ti, x1, 0, startByte - ti);
				buf.append(new String(x1));
				byte[] x2 = new byte[endByte - startByte];
				System.arraycopy(bytes, startByte, x2, 0, endByte - startByte);

				HighlightValue highlightData = findHighlightData(hKey);

				AttributedStringBuilder asb = new AttributedStringBuilder();
				if (highlightData != null) {
					AttributedStyle style = new AttributedStyle();
					if (highlightData.color() > -1) {
						style = style.foregroundRgb(highlightData.color());
					}
					if (highlightData.bold()) {
						style = style.bold();
					}
					if (highlightData.italic()) {
						style = style.italic();
					}
					if (highlightData.underline()) {
						style = style.underline();
					}
					asb.style(style);
				}
				asb.append(new String(x2));
				buf.append(asb.toAnsi());

				ti = endByte;
			}
		}

		if (ti < bytes.length) {
			byte[] x = new byte[bytes.length - ti];
			System.arraycopy(bytes, ti, x, 0, bytes.length - ti);
			buf.append(new String(x));
		}

		// String out = new String(bytes);
		String out = buf.toString();
		return out;
	}

	// void ts_parser_delete(TSParser *self);
	// void ts_tree_delete(TSTree *self);
	// void ts_tree_cursor_delete(TSTreeCursor *self);
	// void ts_query_delete(TSQuery *self);
	// void ts_query_cursor_delete(TSQueryCursor *self);
	// void ts_language_delete(const TSLanguage *self);
	// void ts_lookahead_iterator_delete(TSLookaheadIterator *self);
	// void ts_wasm_store_delete(TSWasmStore *);

	private record HighlightData(String key, int start, int end) {
	}

	private static Map<String, HighlightValue> highlightValues = new HashMap<>();

	private static HighlightValue findHighlightData(String key) {
		HighlightValue v = null;
		int s = 0;
		for (Entry<String, HighlightValue> e : highlightValues.entrySet()) {
			if (e.getKey().startsWith(key)) {
				int size = e.getKey().split("\\.").length;
				if (size > s) {
					v = e.getValue();
					s = size;
				}
			}
		}
		log.debug(String.format("XXX key %s picking %s", key, v));
		return v;
	}

	static {
		highlightValues.put("attribute", new HighlightValue(0xaf0000, false, true, false));
		highlightValues.put("comment", new HighlightValue(0x8a8a8a, false, true, false));
		highlightValues.put("constant.builtin", new HighlightValue(0x875f00, true, false, false));
		highlightValues.put("constant", new HighlightValue(0x875f00, false, false, false));
		highlightValues.put("constructor", new HighlightValue(0xaf8700, false, false, false));
		highlightValues.put("embedded", new HighlightValue(-1, false, false, false));
		highlightValues.put("function.builtin", new HighlightValue(0x005fd7, true, false, false));
		highlightValues.put("function", new HighlightValue(0x005fd7, false, false, false));
		highlightValues.put("keyword", new HighlightValue(0x5f00d7, false, false, false));
		highlightValues.put("number", new HighlightValue(0x875f00, true, false, false));
		highlightValues.put("module", new HighlightValue(0xaf8700, false, false, false));
		highlightValues.put("property", new HighlightValue(0xaf0000, false, false, false));
		highlightValues.put("operator", new HighlightValue(0x4e4e4e, true, false, false));
		highlightValues.put("punctuation.bracket", new HighlightValue(0x4e4e4e, false, false, false));
		highlightValues.put("punctuation.delimiter", new HighlightValue(0x4e4e4e, false, false, false));
		highlightValues.put("string.special", new HighlightValue(0x008787, false, false, false));
		highlightValues.put("string", new HighlightValue(0x008700, false, false, false));
		highlightValues.put("tag", new HighlightValue(0x000087, false, false, false));
		highlightValues.put("type", new HighlightValue(0x005f5f, false, false, false));
		highlightValues.put("type.builtin", new HighlightValue(0x005f5f, true, false, false));
		highlightValues.put("variable.builtin", new HighlightValue(-1, true, false, false));
		highlightValues.put("variable.parameter", new HighlightValue(-1, false, false, true));
	}

	private record HighlightValue(int color, boolean bold, boolean italic, boolean underline) {

		@Override
		public String toString() {
			return String.format("HighlightValue [color=%s, bold=%s, italic=%s, underline=%s]", color, bold, italic, underline);
		}

	}
}
