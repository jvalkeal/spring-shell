package org.springframework.shell.test.jediterm.terminal;

/**
 *
 * @author jediterm authors
 */
public interface TerminalDisplay {

	int getRowCount();

	int getColumnCount();

	void beep();

	void scrollArea(final int scrollRegionTop, final int scrollRegionSize, int dy);

	String getWindowTitle();

	void setWindowTitle(String name);

	boolean ambiguousCharsAreDoubleWidth();

	default void setBracketedPasteMode(boolean enabled) {}

	// default TerminalColor getWindowForeground() {
	// 	return null;
	// }

	// default TerminalColor getWindowBackground() {
	// 	return null;
	// }
}
