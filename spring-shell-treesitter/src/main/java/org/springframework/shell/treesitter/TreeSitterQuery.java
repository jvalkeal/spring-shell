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
package org.springframework.shell.treesitter;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.foreign.ValueLayout.OfInt;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.shell.treesitter.predicate.MatchTreeSitterQueryPredicate;
import org.springframework.shell.treesitter.predicate.TreeSitterQueryPredicate;
import org.springframework.shell.treesitter.predicate.TreeSitterQueryPredicate.TreeSitterQueryPredicateArg;
import org.springframework.shell.treesitter.predicate.TreeSitterQueryPredicate.TreeSitterQueryPredicateContext;
import org.springframework.shell.treesitter.predicate.TreeSitterQueryPredicate.TreeSitterQueryPredicateDefinition;
import org.springframework.shell.treesitter.predicate.TreeSitterQueryPredicate.TreeSitterQueryPredicateType;
import org.springframework.shell.treesitter.ts.TSNode;
import org.springframework.shell.treesitter.ts.TSQueryCapture;
import org.springframework.shell.treesitter.ts.TSQueryMatch;
import org.springframework.shell.treesitter.ts.TSQueryPredicateStep;
import org.springframework.shell.treesitter.ts.TreeSitter;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * {@code treesitter} query implementation.
 *
 * @author Janne Valkealahti
 */
public class TreeSitterQuery implements AutoCloseable {

	private final static Logger log = LoggerFactory.getLogger(TreeSitterQuery.class);
	private TreeSitterLanguage language;
	private String source;
	private final Arena arena;

	public TreeSitterQuery(TreeSitterLanguage language, String source) {
		Assert.notNull(language, "language must be");
		Assert.notNull(source, "source must be");
		arena = Arena.ofShared();
		this.language = language;
		this.source = source;
	}

	@Override
	public void close() {
		arena.close();
	}

	public List<TreeSitterQueryMatch> findMatches(TreeSitterNode node) {
		MemorySegment cursor = TreeSitter.ts_query_cursor_new().reinterpret(arena, TreeSitter::ts_query_cursor_delete);
		MemorySegment languageSegment = language.init();
		MemorySegment sourceSegment = arena.allocateFrom(source);
		int sourceLen = source.length();
		MemorySegment error_offset = arena.allocateFrom(OfInt.JAVA_INT, 0);
		MemorySegment error_type = arena.allocateFrom(OfInt.JAVA_INT, 0);
		MemorySegment querySegment = TreeSitter
				.ts_query_new(languageSegment, sourceSegment, sourceLen, error_offset, error_type)
				.reinterpret(arena, TreeSitter::ts_query_delete);
		TreeSitter.ts_query_cursor_exec(cursor, querySegment, node.getNode());

		MemorySegment queryMatch = TSQueryMatch.allocate(arena);

		List<String> queryCaptureNames = getQueryCaptureNames(querySegment);
		List<String> queryStringValues = getQueryStringValues(querySegment);
		MultiValueMap<Integer, TreeSitterQueryPredicate> queryPredicates = getQueryPredicates(querySegment,
				queryStringValues, queryCaptureNames);
		log.info("XXX queryPredicates={}", queryPredicates);

		List<TreeSitterQueryMatch> matches = new ArrayList<>();

		while (TreeSitter.ts_query_cursor_next_match(cursor, queryMatch)) {
			short patternIndex = TSQueryMatch.pattern_index(queryMatch);
			short count = TSQueryMatch.capture_count(queryMatch);
			int id = TSQueryMatch.id(queryMatch);


			MemorySegment captures = TSQueryMatch.captures(queryMatch);

			List<TreeSitterQueryCapture> queryCaptures = new ArrayList<>();

			List<String> names = new ArrayList<>();
			for (short i = 0; i < count; ++i) {
				MemorySegment capture = TSQueryCapture.asSlice(captures, i);
				String name = queryCaptureNames.get(TSQueryCapture.index(capture));
				MemorySegment captureNode = TSNode.allocate(arena).copyFrom(TSQueryCapture.node(capture));
				TreeSitterNode treeSitterNode = TreeSitterNode.of(captureNode, node.getTree());
				TreeSitterQueryCapture treeSitterQueryCapture = new TreeSitterQueryCapture(treeSitterNode, i, name);
				queryCaptures.add(treeSitterQueryCapture);
				names.add(name);
			}

			int index = TSQueryCapture.index(captures);
			TreeSitterQueryMatch treeSitterQueryMatch = new TreeSitterQueryMatch(id, patternIndex, index, queryCaptureNames.size(),
					queryCaptures, names);
			List<TreeSitterQueryPredicate> pList = queryPredicates.get(treeSitterQueryMatch.getPatternIndex());
			boolean add = true;
			if (pList != null) {
				TreeSitterQueryPredicateContext context = new TreeSitterQueryPredicateContext(treeSitterQueryMatch);
				add = pList.stream().allMatch(p -> p.test(context));
			}

			if (add) {
				matches.add(treeSitterQueryMatch);
			}
		}

		return matches;
	}

	private List<String> getQueryCaptureNames(MemorySegment querySegment) {
		int captureCount = TreeSitter.ts_query_capture_count(querySegment);
		List<String> captureNames = new ArrayList<>(captureCount);
		try (var alloc = Arena.ofConfined()) {
			for (int i = 0; i < captureCount; i++) {
				MemorySegment length = arena.allocate(ValueLayout.JAVA_INT);
				MemorySegment name = TreeSitter.ts_query_capture_name_for_id(querySegment, i, length);
				captureNames.add(name.getString(0));
			}
		}
		return captureNames;
	}

	private List<String> getQueryStringValues(MemorySegment querySegment) {
		int stringCount = TreeSitter.ts_query_string_count(querySegment);
		List<String> stringValues = new ArrayList<>(stringCount);
		try (var alloc = Arena.ofConfined()) {
			for (int i = 0; i < stringCount; ++i) {
				MemorySegment length = alloc.allocate(ValueLayout.JAVA_INT);
				MemorySegment name = TreeSitter.ts_query_string_value_for_id(querySegment, i, length);
				stringValues.add(name.getString(0));
			}
		}
		return stringValues;
	}

	private MultiValueMap<Integer, TreeSitterQueryPredicate> getQueryPredicates(MemorySegment querySegment, List<String> stringValues, List<String> captureNames) {
		MultiValueMap<Integer, TreeSitterQueryPredicate> predicates = new LinkedMultiValueMap<>();
		try (Arena alloc = Arena.ofConfined()) {
			int patternCount = TreeSitter.ts_query_pattern_count(querySegment);
			for (int patternIndex = 0; patternIndex < patternCount; patternIndex++) {
				MemorySegment count = alloc.allocate(OfInt.JAVA_INT);
				MemorySegment tokens = TreeSitter.ts_query_predicates_for_pattern(querySegment, patternIndex, count);
				int steps = count.get(OfInt.JAVA_INT, 0);
				log.info("XXX pattern predicate - patternIndex={} steps={}", patternIndex, steps);

				List<TreeSitterQueryPredicateDefinition> defs = new ArrayList<>();
				List<TreeSitterQueryPredicateArg> args = null;
				String predicateValue = null;
				for (long stepIndex = 0; stepIndex < steps; ++stepIndex) {
					if (args == null) {
						args = new ArrayList<>();
					}
					MemorySegment t = TSQueryPredicateStep.asSlice(tokens, stepIndex);
					int type = TSQueryPredicateStep.type(t);
					if (type == TreeSitter.TSQueryPredicateStepTypeDone()) {
						log.info("XXX pattern steps -  patternIndex={} type={} done", patternIndex, type);
						defs.add(new TreeSitterQueryPredicateDefinition(predicateValue, args));
						if (MatchTreeSitterQueryPredicate.PREDICATE.equals(predicateValue)) {
							log.info("XXX should add {}", predicateValue);
							TreeSitterQueryPredicate p = MatchTreeSitterQueryPredicate.of(args);
							predicates.add(patternIndex, p);
						}
						args = null;
						predicateValue = null;
						continue;
					}
					else if (type == TreeSitter.TSQueryPredicateStepTypeCapture()) {
						String value = captureNames.get(TSQueryPredicateStep.value_id(t));
						args.add(new TreeSitterQueryPredicateArg(value, TreeSitterQueryPredicateType.CAPTURE));
						log.info("XXX pattern steps -  patternIndex={} type={} value={}", patternIndex, type, value);
					}
					else if (type == TreeSitter.TSQueryPredicateStepTypeString()) {
						if (predicateValue == null) {
							predicateValue = stringValues.get(TSQueryPredicateStep.value_id(t));
						}
						else {
							String value = stringValues.get(TSQueryPredicateStep.value_id(t));
							args.add(new TreeSitterQueryPredicateArg(value, TreeSitterQueryPredicateType.STRING));
							log.info("XXX pattern steps -  patternIndex={} type={} value={}", patternIndex, type, value);
						}
					}
				}
			}
		}
		return predicates;
	}

}
