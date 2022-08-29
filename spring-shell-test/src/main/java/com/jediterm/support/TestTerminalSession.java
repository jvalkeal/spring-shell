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

	private final TestTerminal myTerminal;
	private final TextProcessing myTextProcessing;
	private final TerminalTextBuffer myTerminalTextBuffer;
	private final TextStyle myDefaultStyle;

	public TestTerminalSession(int width, int height) {
	  StyleState state = new StyleState();
	  myDefaultStyle = state.getCurrent();
	  TextStyle hyperlinkTextStyle = new TextStyle(TerminalColor.awt(Color.BLUE), TerminalColor.WHITE);
	  myTextProcessing = new TextProcessing(hyperlinkTextStyle, HyperlinkStyle.HighlightMode.ALWAYS);
	  myTerminalTextBuffer = new TerminalTextBuffer(width, height, state, myTextProcessing);
	  myTextProcessing.setTerminalTextBuffer(myTerminalTextBuffer);
	  myTerminal = new TestTerminal(myTerminalTextBuffer, state);
	}

	public TestTerminal getTerminal() {
	  return myTerminal;
	}

	public TestTerminalDisplay getDisplay() {
	  return myTerminal.getDisplay();
	}

	public TerminalTextBuffer getTerminalTextBuffer() {
	  return myTerminalTextBuffer;
	}

	public TextProcessing getTextProcessing() {
	  return myTextProcessing;
	}

	public TextStyle getDefaultStyle() {
	  return myDefaultStyle;
	}

	public void process(String data) throws IOException {
	  ArrayTerminalDataStream fileStream = new ArrayTerminalDataStream(data.toCharArray());
	  Emulator emulator = new JediEmulator(fileStream, myTerminal);

	  while (emulator.hasNext()) {
		emulator.next();
	  }
	}
}
