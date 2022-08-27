package com.jediterm.support;

import com.jediterm.terminal.TerminalOutputStream;
import com.jediterm.terminal.model.TerminalTextBuffer;
import com.jediterm.terminal.model.JediTerminal;
import com.jediterm.terminal.model.StyleState;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author traff
 */
public class TestTerminal extends JediTerminal {
  private final TestTerminalDisplay myBufferDisplay;
  private final TerminalTextBuffer myTextBuffer;
  private ByteArrayOutputStream myOutputStream;

  public TestTerminal(TerminalTextBuffer terminalTextBuffer,
                            StyleState initialStyleState) {
    this(new TestTerminalDisplay(terminalTextBuffer), terminalTextBuffer, initialStyleState);
    myOutputStream = new ByteArrayOutputStream();
    setTerminalOutput(new TestOutputStream(myOutputStream));
  }

  private TestTerminal( TestTerminalDisplay bufferDisplay,
                              TerminalTextBuffer terminalTextBuffer,
                              StyleState initialStyleState) {
    super(bufferDisplay, terminalTextBuffer, initialStyleState);
    myBufferDisplay = bufferDisplay;
    myTextBuffer = terminalTextBuffer;
  }

  public  TestTerminalDisplay getDisplay() {
    return myBufferDisplay;
  }

  public  TerminalTextBuffer getTextBuffer() {
    return myTextBuffer;
  }

  public  String getOutputAndClear() {
    String output = myOutputStream.toString(StandardCharsets.UTF_8);
    myOutputStream.reset();
    return output;
  }

  private static class TestOutputStream implements TerminalOutputStream {
    private final OutputStream myOutputStream;

    public TestOutputStream( OutputStream outputStream) {
      myOutputStream = outputStream;
    }

    @Override
    public void sendBytes(byte[] response) {
      try {
        myOutputStream.write(response);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void sendString(String string) {
      try {
        myOutputStream.write(string.getBytes(StandardCharsets.UTF_8));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
