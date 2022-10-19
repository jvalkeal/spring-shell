package org.springframework.shell.test.jediterm.terminal.model;

public class SubCharBuffer extends CharBuffer {
  private final CharBuffer myParent;
  private final int myOffset;

  public SubCharBuffer( CharBuffer parent, int offset, int length) {
    super(parent.getBuf(), parent.getStart() + offset, length);
    myParent = parent;
    myOffset = offset;
  }

  public  CharBuffer getParent() {
    return myParent;
  }

  public int getOffset() {
    return myOffset;
  }
}
