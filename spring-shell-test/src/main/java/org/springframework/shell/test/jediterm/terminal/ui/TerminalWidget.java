package org.springframework.shell.test.jediterm.terminal.ui;

import org.springframework.shell.test.jediterm.terminal.TerminalDisplay;
import org.springframework.shell.test.jediterm.terminal.TtyConnector;

/**
 * @author traff
 */
public interface TerminalWidget {

	JediTermWidget createTerminalSession(TtyConnector ttyConnector);

	boolean canOpenSession();

	void setTerminalPanelListener(TerminalPanelListener terminalPanelListener);

	TerminalSession getCurrentSession();

	TerminalDisplay getTerminalDisplay();
}
