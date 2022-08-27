package org.springframework.shell.test;

import java.io.IOException;

import com.jediterm.support.TestTerminalSession;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class XxxTests {

	@Test
	void xxx1() throws IOException {
		TestTerminalSession session = new TestTerminalSession(30, 3);
		session.process("hi\u001B[1Dx");
		assertThat(session.getTerminal().getTextBuffer().getScreenLines().trim()).isEqualTo("hx");
	}
}
