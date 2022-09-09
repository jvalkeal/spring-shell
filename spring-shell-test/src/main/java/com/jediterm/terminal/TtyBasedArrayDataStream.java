package com.jediterm.terminal;

import java.io.IOException;

import com.jediterm.terminal.util.CharUtils;

/**
 * Takes data from and sends it back to TTY input and output streams via {@link TtyConnector}
 *
 * @author jediterm authors
 */
public class TtyBasedArrayDataStream extends ArrayTerminalDataStream {

	private final TtyConnector ttyConnector;
	private final Runnable myOnBeforeBlockingWait;

	public TtyBasedArrayDataStream(final TtyConnector ttyConnector, final Runnable onBeforeBlockingWait) {
		super(new char[1024], 0, 0);
		this.ttyConnector = ttyConnector;
		myOnBeforeBlockingWait = onBeforeBlockingWait;
	}

	public TtyBasedArrayDataStream(final TtyConnector ttyConnector) {
		super(new char[1024], 0, 0);
		this.ttyConnector = ttyConnector;
		myOnBeforeBlockingWait = null;
	}

	@Override
	public char getChar() throws IOException {
		if (length == 0) {
			fillBuf();
		}
		return super.getChar();
	}

	@Override
	public String readNonControlCharacters(int maxChars) throws IOException {
		if (length == 0) {
			fillBuf();
		}

		return super.readNonControlCharacters(maxChars);
	}

	@Override
	public String toString() {
		return CharUtils.toHumanReadableText(new String(buf, offset, length));
	}

	private void fillBuf() throws IOException {
		offset = 0;

		if (!this.ttyConnector.ready() && myOnBeforeBlockingWait != null) {
			myOnBeforeBlockingWait.run();
		}
		length = this.ttyConnector.read(buf, offset, buf.length);

		if (length <= 0) {
			length = 0;
			throw new EOF();
		}
	}
}
