package org.springframework.shell.textmate;

public interface IGrammarRepository {

	IRawGrammar lookup(String scopeName);
	String[] injections(String scopeName);

}
