package com.jediterm.terminal.ui.settings;

import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.TerminalActionPresentation;
import com.jediterm.terminal.ui.UIUtil;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * @author traff
 */
public class DefaultTabbedSettingsProvider extends DefaultSettingsProvider implements TabbedSettingsProvider {
  @Override
  public boolean shouldCloseTabOnLogout(TtyConnector ttyConnector) {
    return true;
  }

  @Override
  public String tabName(TtyConnector ttyConnector, String sessionName) {
    return sessionName;
  }

  @Override
  public TerminalActionPresentation getPreviousTabActionPresentation() {
    return new TerminalActionPresentation("Previous Tab", UIUtil.isMac
      ? KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_DOWN_MASK)
      : KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.ALT_DOWN_MASK));
  }

  @Override
  public TerminalActionPresentation getNextTabActionPresentation() {
    return new TerminalActionPresentation("Next Tab", UIUtil.isMac
      ? KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_DOWN_MASK)
      : KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_DOWN_MASK));
  }
}
