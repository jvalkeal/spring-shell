package org.springframework.shell.test.jediterm.terminal.ui;

import org.springframework.shell.test.jediterm.terminal.RequestOrigin;

public interface TerminalPanelListener {
	void onPanelResize(RequestOrigin origin);

	void onTitleChanged(String title);
}
