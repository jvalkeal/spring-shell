package org.springframework.shell.test;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;

import com.jediterm.support.TestTerminalSession;
import com.jediterm.terminal.Questioner;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class XxxTests {

	// @Test
	void xxx1() throws IOException {
		TestTerminalSession session = new TestTerminalSession(30, 3);
		session.process("hi\u001B[1Dx");
		assertThat(session.getTerminal().getTextBuffer().getScreenLines().trim()).isEqualTo("hx");
	}

	@Test
	void xxx2() throws Exception {
		JediTermWidget widget = createTerminalWidget();

		// JFrame frame = new JFrame("Basic Terminal Example");
		// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// frame.setContentPane(widget);
		// frame.pack();
		// frame.setVisible(true);

		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			String screen = widget.getTerminalTextBuffer().getScreenLines();
			assertThat(screen).contains("Hello");
		});


		// String screen = widget.getTerminalTextBuffer().getScreenLines();
		// assertThat(screen).contains("Hello");
	}


	private static JediTermWidget createTerminalWidget() {
		JediTermWidget widget = new JediTermWidget(80, 24, new DefaultSettingsProvider());
		PipedWriter terminalWriter = new PipedWriter();
		widget.setTtyConnector(new ExampleTtyConnector(terminalWriter));
		widget.start();
		try {
		  writeTerminalCommands(terminalWriter);
		  terminalWriter.close();
		} catch (IOException e) {
		  e.printStackTrace();
		}
		return widget;
	  }

	  private static void writeTerminalCommands(PipedWriter writer) throws IOException {
		char ESC = 27;
		writer.write("Hello\r\n");
		// writer.write(ESC + "%G");
		// writer.write(ESC + "[31m");
		// writer.write("Hello\r\n");
		// writer.write(ESC + "[32;43m");
		// writer.write("World\r\n");
	  }

	  private static class ExampleTtyConnector implements TtyConnector {

		private final PipedReader myReader;

		public ExampleTtyConnector(PipedWriter writer) {
		  try {
			myReader =  new PipedReader(writer);
		  } catch (IOException e) {
			throw new RuntimeException(e);
		  }
		}

		@Override
		public boolean init(Questioner q) {
		  return true;
		}

		@Override
		public void close() {
		}

		@Override
		public String getName() {
		  return null;
		}

		@Override
		public int read(char[] buf, int offset, int length) throws IOException {
		  return myReader.read(buf, offset, length);
		}

		@Override
		public void write(byte[] bytes) {
		}

		@Override
		public boolean isConnected() {
		  return true;
		}

		@Override
		public void write(String string) {
		}

		@Override
		public int waitFor() {
		  return 0;
		}

		@Override
		public boolean ready() throws IOException {
		  return myReader.ready();
		}


	  }

}
