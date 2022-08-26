package com.jediterm.terminal.ui.settings;

import com.jediterm.terminal.ui.TerminalActionPresentation;

public interface SystemSettingsProvider {
  TerminalActionPresentation getNewSessionActionPresentation();

  TerminalActionPresentation getOpenUrlActionPresentation();

  TerminalActionPresentation getCopyActionPresentation();

  TerminalActionPresentation getPasteActionPresentation();

  TerminalActionPresentation getClearBufferActionPresentation();

  TerminalActionPresentation getPageUpActionPresentation();

  TerminalActionPresentation getPageDownActionPresentation();

  TerminalActionPresentation getLineUpActionPresentation();

  TerminalActionPresentation getLineDownActionPresentation();

  TerminalActionPresentation getCloseSessionActionPresentation();

  TerminalActionPresentation getFindActionPresentation();

  TerminalActionPresentation getSelectAllActionPresentation();
}
