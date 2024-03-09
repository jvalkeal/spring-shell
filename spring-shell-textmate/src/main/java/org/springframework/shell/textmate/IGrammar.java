package org.springframework.shell.textmate;

import java.time.Duration;

public interface IGrammar {

	ITokenizeLineResult tokenizeLine(String lineText, StateStack prevState, Duration timeLimit);
}
