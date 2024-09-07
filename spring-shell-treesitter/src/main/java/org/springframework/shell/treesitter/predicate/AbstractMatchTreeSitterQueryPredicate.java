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
package org.springframework.shell.treesitter.predicate;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.shell.treesitter.TreeSitterNode;
import org.springframework.shell.treesitter.TreeSitterQueryMatch;
import org.springframework.shell.treesitter.TreeSitterTree;

/**
 * Base implementation of a {@code treesitter} {@code match} predicates.
 *
 * @author Janne Valkealahti
 */
abstract class AbstractMatchTreeSitterQueryPredicate extends AbstractTreeSitterQueryPredicate {

	private String capture;
	private List<Pattern> patterns;
	private boolean matchAll = false;
	private boolean negate = false;

	AbstractMatchTreeSitterQueryPredicate(String capture, Pattern pattern, boolean matchAll, boolean negate) {
		this(capture, List.of(pattern), matchAll, negate);
	}

	AbstractMatchTreeSitterQueryPredicate(String capture, List<Pattern> patterns, boolean matchAll, boolean negate) {
		this.capture = capture;
		this.patterns = patterns;
		this.matchAll = matchAll;
		this.negate = negate;
	}

	@Override
	protected boolean testInternal(TreeSitterQueryPredicateContext context) {
		TreeSitterQueryMatch match = context.match();

		List<TreeSitterNode> nodes = match.getCaptures().stream()
			.filter(c -> c.getName().equals(capture))
			.map(c -> c.getNode())
			.collect(Collectors.toList())
			;

		boolean present = nodes.stream()
			.filter(n -> {
				TreeSitterTree tree = n.getTree();
				byte[] xxx = new byte[n.getEndByte() - n.getStartByte()];
				System.arraycopy(tree.getContent(), n.getStartByte(), xxx, 0, xxx.length);
				String ddd = new String(xxx);
				boolean matches = patternMatch(ddd);
				return matches;
			})
			.findFirst()
			.isPresent()
			;

		if (!negate) {
			return present;
		}
		else {
			return !present;
		}
	}

	private boolean patternMatch(String text) {
		Stream<Pattern> pStream = patterns.stream();
		if (matchAll) {
			return pStream.allMatch(x -> x.matcher(text).matches());
		}
		else {
			return pStream.anyMatch(x -> x.matcher(text).matches());
		}
		// for (Pattern p : patterns) {
		// 	// boolean matches = p.matcher(text).matches();
		// 	// if (matches) {
		// 	// 	return true;
		// 	// }
		// }
		// return false;
	}

}
