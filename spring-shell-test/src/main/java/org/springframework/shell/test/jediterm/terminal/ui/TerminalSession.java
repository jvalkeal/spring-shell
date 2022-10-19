package org.springframework.shell.test.jediterm.terminal.ui;

import org.springframework.shell.test.jediterm.terminal.Terminal;
import org.springframework.shell.test.jediterm.terminal.TerminalStarter;
import org.springframework.shell.test.jediterm.terminal.TtyConnector;
import org.springframework.shell.test.jediterm.terminal.model.TerminalTextBuffer;

/**
 * @author traff
 */
public interface TerminalSession {

	void start();

	TerminalTextBuffer getTerminalTextBuffer();

	TerminalStarter getTerminalStarter();

	Terminal getTerminal();

	TtyConnector getTtyConnector();

	String getSessionName();

	void close();
}
