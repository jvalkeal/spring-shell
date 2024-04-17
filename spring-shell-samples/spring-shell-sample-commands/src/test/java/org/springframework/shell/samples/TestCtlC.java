package org.springframework.shell.samples;

import java.io.IOException;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.Log;

public class TestCtlC {

    public static void main(String[] args) throws IOException {
        TestCtlC tester = new TestCtlC();
        tester.processInput();
    }

    private final Terminal terminal;
    private final LineReader reader;
    private boolean interrupted = false;

    public TestCtlC() throws IOException {
        terminal = TerminalBuilder.builder().build();
        Log.info(terminal.getClass().getSimpleName());
        terminal.handle(Terminal.Signal.INT, signal -> {
            interrupted = true;
            Log.info("interrupt");
        });
        reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .build();
    }

    public void processInput() {
        while (true) {
            try {
                interrupted = false;
                String line = reader.readLine("TESTCTLC> ");
                if (line.equalsIgnoreCase("QUIT")) {
                    System.exit(0);
                } else if (line.equalsIgnoreCase("SLEEP")) {
                    for (int c = 0; !interrupted && c < 1000; c++) {
                        Thread.sleep(10);
                    }
                }
            } catch (UserInterruptException | EndOfFileException | InterruptedException ex) {
                Log.error(ex.getClass().getSimpleName());
            }
        }
    }
}
