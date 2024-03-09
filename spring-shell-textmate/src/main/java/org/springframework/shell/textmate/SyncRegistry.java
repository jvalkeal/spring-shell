package org.springframework.shell.textmate;

import java.util.HashMap;
import java.util.Map;

public class SyncRegistry implements IGrammarRepository, IThemeProvider {

	private Theme theme;
	private Map<String, Grammar> grammars = new HashMap<>();
	private Map<String, IRawGrammar> rawGrammars = new HashMap<>();
	private Map<String, String[]> injectionGrammars = new HashMap<>();

	public SyncRegistry(Theme theme) {
		this.theme = theme;
	}

	public void addGrammar(IRawGrammar grammar, String[] injectionScopeNames) {
		rawGrammars.put(grammar.scopeName(), grammar);
		if (injectionScopeNames != null && injectionScopeNames.length > 0) {
			injectionGrammars.put(grammar.scopeName(), injectionScopeNames);
		}
	}

	@Override
	public IRawGrammar lookup(String scopeName) {
		return rawGrammars.get(scopeName);
	}

	@Override
	public String[] injections(String scopeName) {
		return injectionGrammars.get(scopeName);
	}

	@Override
	public StyleAttributes themeMatch(ScopeStack scopeStack) {

		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'themeMatch'");
	}

	@Override
	public StyleAttributes getDefaults() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getDefaults'");
	}

}
