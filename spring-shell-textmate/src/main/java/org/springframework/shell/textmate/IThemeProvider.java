package org.springframework.shell.textmate;

public interface IThemeProvider {

	StyleAttributes themeMatch(ScopeStack scopeStack);
	StyleAttributes getDefaults();

}
