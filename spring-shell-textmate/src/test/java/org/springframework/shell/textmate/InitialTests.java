package org.springframework.shell.textmate;

import org.junit.jupiter.api.Test;

class InitialTests {

	@Test
	void xxx() {
		Registry registry = new Registry(null);
		IGrammar grammar = registry.addGrammar(null);
		ITokenizeLineResult result = grammar.tokenizeLine(null, null, null);
	}
}
