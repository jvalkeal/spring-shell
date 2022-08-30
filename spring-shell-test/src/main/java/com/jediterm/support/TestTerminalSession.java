package com.jediterm.support;

import java.awt.Color;
import java.io.IOException;

import com.jediterm.terminal.ArrayTerminalDataStream;
import com.jediterm.terminal.HyperlinkStyle;
import com.jediterm.terminal.TerminalColor;
import com.jediterm.terminal.TextStyle;
import com.jediterm.terminal.emulator.Emulator;
import com.jediterm.terminal.emulator.JediEmulator;
import com.jediterm.terminal.model.StyleState;
import com.jediterm.terminal.model.TerminalTextBuffer;
import com.jediterm.terminal.model.hyperlinks.TextProcessing;

/**
 *
 * @author jediterm authors
 */
public class TestTerminalSession {

	private final TestTerminal terminal;
	private final TextProcessing textProcessing;
	private final TerminalTextBuffer terminalTextBuffer;
	private final TextStyle textStyle;

	public TestTerminalSession(int width, int height) {
	  StyleState state = new StyleState();
	  this.textStyle = state.getCurrent();
	  TextStyle hyperlinkTextStyle = new TextStyle(TerminalColor.awt(Color.BLUE), TerminalColor.WHITE);
	  this.textProcessing = new TextProcessing(hyperlinkTextStyle, HyperlinkStyle.HighlightMode.ALWAYS);
	  this.terminalTextBuffer = new TerminalTextBuffer(width, height, state, this.textProcessing);
	  this.textProcessing.setTerminalTextBuffer(this.terminalTextBuffer);
	  this.terminal = new TestTerminal(this.terminalTextBuffer, state);
	}

	public TestTerminal getTerminal() {
	  return this.terminal;
	}

	public TestTerminalDisplay getDisplay() {
	  return this.terminal.getDisplay();
	}

	public TerminalTextBuffer getTerminalTextBuffer() {
	  return this.terminalTextBuffer;
	}

	public TextProcessing getTextProcessing() {
	  return this.textProcessing;
	}

	public TextStyle getDefaultStyle() {
	  return this.textStyle;
	}

	public void process(String data) throws IOException {
	  ArrayTerminalDataStream fileStream = new ArrayTerminalDataStream(data.toCharArray());
	  Emulator emulator = new JediEmulator(fileStream, this.terminal);

	  while (emulator.hasNext()) {
		emulator.next();
	  }
	}
}
