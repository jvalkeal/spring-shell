package com.jediterm.terminal.ui;

import com.jediterm.terminal.RequestOrigin;

public interface TerminalPanelListener {
	void onPanelResize(RequestOrigin origin);

	void onTitleChanged(String title);
}
