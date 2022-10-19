package com.jediterm.terminal.ui;

import com.jediterm.terminal.Terminal;
import com.jediterm.terminal.TerminalStarter;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.model.TerminalTextBuffer;

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
