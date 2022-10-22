package org.springframework.shell.test.jediterm.terminal;

import java.io.IOException;

/**
 * Interface to tty.
 *
 * @author jediterm authors
 */
public interface TtyConnector {

	boolean init();

	void close();

	default void resize(int width, int height) {
		// support old implementations not overriding this method
		resize(width, height);
		// StackOverflowError is only possible if both resize(Dimension) and resize(Dimension,Dimension) are not overridden.
	}

	// /**
	//  * @deprecated use {@link #resize(Dimension)} instead
	//  */
	// @SuppressWarnings("unused")
	// @Deprecated
	// default void resize(int width, int height, int pixelSizeWidth, int pixelSizeHeight) {
	// 	// support old code that calls this method on new implementations (not overriding this deprecated method)
	// 	resize(width, height);
	// }

	String getName();

	int read(char[] buf, int offset, int length) throws IOException;

	void write(byte[] bytes) throws IOException;

	boolean isConnected();

	void write(String string) throws IOException;

	int waitFor() throws InterruptedException;

	boolean ready() throws IOException;
}
