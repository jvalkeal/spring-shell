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

/**
 * Implementation of a {@code treesitter} {@code match} predicate.
 *
 * @author Janne Valkealahti
 */
public class MatchTreeSitterQueryPredicate extends AbstractMatchTreeSitterQueryPredicate {

	public MatchTreeSitterQueryPredicate(String capture, Pattern pattern) {
		super(capture, pattern, true, false);
	}

	public static MatchTreeSitterQueryPredicate of(List<TreeSitterQueryPredicate.TreeSitterQueryPredicateArg> args) {
		if (args.size() == 2) {
			TreeSitterQueryPredicateArg captureArg = args.get(0);
			TreeSitterQueryPredicateArg patternArg = args.get(1);
			return new MatchTreeSitterQueryPredicate(captureArg.value(), Pattern.compile(patternArg.value()));
		}
		else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public String toString() {
		return "MatchTreeSitterQueryPredicate [%s]".formatted(super.toString());
	}

}
