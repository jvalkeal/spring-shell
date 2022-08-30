package com.jediterm.terminal;

import java.awt.Dimension;

import com.jediterm.terminal.emulator.mouse.MouseMode;
import com.jediterm.terminal.model.JediTerminal;
import com.jediterm.terminal.model.TerminalSelection;

/**
 *
 * @author jediterm authors
 */
public interface TerminalDisplay {

	int getRowCount();

	int getColumnCount();

	void setCursor(int x, int y);

	void setCursorShape(CursorShape shape);

	void beep();

	void requestResize(Dimension newWinSize, RequestOrigin origin, int cursorX, int cursorY,
			JediTerminal.ResizeHandler resizeHandler);

	void scrollArea(final int scrollRegionTop, final int scrollRegionSize, int dy);

	void setCursorVisible(boolean shouldDrawCursor);

	void setScrollingEnabled(boolean enabled);

	void setBlinkingCursor(boolean enabled);

	String getWindowTitle();

	void setWindowTitle(String name);

	void terminalMouseModeSet(MouseMode mode);

	TerminalSelection getSelection();

	boolean ambiguousCharsAreDoubleWidth();

	default void setBracketedPasteMode(boolean enabled) {}

	default TerminalColor getWindowForeground() {
		return null;
	}

	default TerminalColor getWindowBackground() {
		return null;
	}
}
