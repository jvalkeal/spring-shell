package com.jediterm.terminal.ui;

import com.jediterm.terminal.TerminalDisplay;
import com.jediterm.terminal.TtyConnector;

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
