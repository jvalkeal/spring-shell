package org.springframework.shell.test.jediterm.terminal;

import org.springframework.shell.test.jediterm.terminal.model.CharBuffer;

/**
 *
 * @author jediterm authors
 */
public class StyledTextConsumerAdapter implements StyledTextConsumer {

	public void consume(int x, int y, TextStyle style, CharBuffer characters, int startRow) {
		// to override
	}

	@Override
	public void consumeNul(int x, int y, int nulIndex, TextStyle style, CharBuffer characters, int startRow) {
		// to override
	}

	@Override
	public void consumeQueue(int x, int y, int nulIndex, int startRow) {
		// to override
	}

}