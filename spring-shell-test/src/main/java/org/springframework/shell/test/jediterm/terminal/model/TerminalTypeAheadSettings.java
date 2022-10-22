package org.springframework.shell.test.jediterm.terminal.model;

import java.util.concurrent.TimeUnit;

import org.springframework.shell.test.jediterm.terminal.TextStyle;

public final class TerminalTypeAheadSettings {

  public static final TerminalTypeAheadSettings DEFAULT = new TerminalTypeAheadSettings(
    true,
    TimeUnit.MILLISECONDS.toNanos(100),
    new TextStyle(null)
  );

  private final boolean myEnabled;
  private final long myLatencyThreshold;
  private final TextStyle myTypeAheadStyle;

  public TerminalTypeAheadSettings(boolean enabled, long latencyThreshold, TextStyle typeAheadColor) {
    myEnabled = enabled;
    myLatencyThreshold = latencyThreshold;
    myTypeAheadStyle = typeAheadColor;
  }

  public boolean isEnabled() {
    return myEnabled;
  }

  public long getLatencyThreshold() {
    return myLatencyThreshold;
  }

  public TextStyle getTypeAheadStyle() {
    return myTypeAheadStyle;
  }
}
