package com.jediterm.terminal.model;

import com.jediterm.terminal.Terminal;
import com.jediterm.terminal.TextStyle;
import com.jediterm.terminal.ui.settings.SettingsProvider;
import com.jediterm.typeahead.TypeAheadTerminalModel;

public class JediTermTypeAheadModel implements TypeAheadTerminalModel {
  private final  Terminal myTerminal;
  private final  TerminalTextBuffer myTerminalTextBuffer;
  private final  SettingsProvider mySettingsProvider;
  private  TypeAheadTerminalModel.ShellType myShellType = ShellType.Unknown;

  private boolean isPredictionsApplied = false;

  public JediTermTypeAheadModel( Terminal terminal,
                                 TerminalTextBuffer textBuffer,
                                 SettingsProvider settingsProvider) {
    myTerminal = terminal;
    myTerminalTextBuffer = textBuffer;
    mySettingsProvider = settingsProvider;
  }

  @Override
  public void insertCharacter(char ch, int index) {
    isPredictionsApplied = true;
    TerminalLine typeAheadLine = getTypeAheadLine();

    TextStyle typeAheadStyle = mySettingsProvider.getTypeAheadSettings().getTypeAheadStyle();
    typeAheadLine.insertString(index, new CharBuffer(ch, 1), typeAheadStyle);

    setTypeAheadLine(typeAheadLine);
  }

  @Override
  public void removeCharacters(int from, int count) {
    isPredictionsApplied = true;
    TerminalLine typeAheadLine = getTypeAheadLine();

    typeAheadLine.deleteCharacters(from, count, TextStyle.EMPTY);

    setTypeAheadLine(typeAheadLine);
  }

  public void forceRedraw() {
    myTerminalTextBuffer.fireTypeAheadModelChangeEvent();
  }

  @Override
  public void moveCursor(int index) {}

  @Override
  public void clearPredictions() {
    if (isPredictionsApplied) {
      myTerminalTextBuffer.clearTypeAheadPredictions();
    }
    isPredictionsApplied = false;
  }

  @Override
  public void lock() {
    myTerminalTextBuffer.lock();
  }

  @Override
  public void unlock() {
    myTerminalTextBuffer.unlock();
  }

  @Override
  public boolean isUsingAlternateBuffer() {
    return myTerminalTextBuffer.isUsingAlternateBuffer();
  }

  @Override
  public boolean isTypeAheadEnabled() {
    return mySettingsProvider.getTypeAheadSettings().isEnabled();
  }

  @Override
  public long getLatencyThreshold() {
    return mySettingsProvider.getTypeAheadSettings().getLatencyThreshold();
  }

  @Override
  public  ShellType getShellType() {
    return myShellType;
  }

  public void setShellType(ShellType shellType) {
    myShellType = shellType;
  }

  @Override
  public  TypeAheadTerminalModel.LineWithCursorX getCurrentLineWithCursor() {
    TerminalLine terminalLine = myTerminalTextBuffer.getLine(myTerminal.getCursorY() - 1);
    return new LineWithCursorX(new StringBuffer(terminalLine.getText()), myTerminal.getCursorX() - 1);
  }

  @Override
  public int getTerminalWidth() {
    return myTerminal.getTerminalWidth();
  }

  private  TerminalLine getTypeAheadLine() {
    TerminalLine terminalLine = myTerminalTextBuffer.getLine(myTerminal.getCursorY() - 1);
    if (terminalLine.myTypeAheadLine != null) {
      terminalLine = terminalLine.myTypeAheadLine;
    }
    return terminalLine.copy();
  }

  private void setTypeAheadLine( TerminalLine typeAheadTerminalLine) {
    TerminalLine terminalLine = myTerminalTextBuffer.getLine(myTerminal.getCursorY() - 1);
    terminalLine.myTypeAheadLine = typeAheadTerminalLine;
  }
}
