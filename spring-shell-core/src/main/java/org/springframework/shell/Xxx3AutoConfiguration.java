package org.springframework.shell;

import java.io.IOException;
import java.nio.file.Paths;

import org.jline.reader.Completer;
import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.Parser;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

@Configuration
public class Xxx3AutoConfiguration {

	@Autowired
	private Terminal terminal;

	@Autowired
	private Completer completer;

	@Autowired
	private Parser parser;

	@Autowired
	private CommandRegistry commandRegistry;

	@Autowired
	private org.jline.reader.History jLineHistory;

	@EventListener
	public void onContextClosedEvent(ContextClosedEvent event) throws IOException {
		jLineHistory.save();
	}

	@Value("${spring.application.name:spring-shell}.log")
	private String historyPath;

	@Bean
	public LineReader lineReader() {
		LineReaderBuilder lineReaderBuilder = LineReaderBuilder.builder()
				.terminal(terminal)
				.appName("Spring Shell")
				.completer(completer)
				.history(jLineHistory)
				.highlighter(new Highlighter() {

					@Override
					public AttributedString highlight(LineReader reader, String buffer) {
						int l = 0;
						String best = null;
						for (String command : commandRegistry.listCommands().keySet()) {
							if (buffer.startsWith(command) && command.length() > l) {
								l = command.length();
								best = command;
							}
						}
						if (best != null) {
							return new AttributedStringBuilder(buffer.length()).append(best, AttributedStyle.BOLD).append(buffer.substring(l)).toAttributedString();
						}
						else {
							return new AttributedString(buffer, AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
						}
					}
				})
				.parser(parser);

		LineReader lineReader = lineReaderBuilder.build();
		lineReader.setVariable(LineReader.HISTORY_FILE, Paths.get(historyPath));
		lineReader.unsetOpt(LineReader.Option.INSERT_TAB); // This allows completion on an empty buffer, rather than inserting a tab
		jLineHistory.attach(lineReader);
		return lineReader;
	}

}
