package com.jediterm.terminal;

/**
 *
 * @author jediterm authors
 */
public interface TerminalCopyPasteHandler {
  void setContents(String text, boolean useSystemSelectionClipboardIfAvailable);

  String getContents(boolean useSystemSelectionClipboardIfAvailable);
}
