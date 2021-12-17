package org.springframework.shell;

import java.util.List;
import java.util.stream.Collectors;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Xxx4AutoConfiguration {

	@Autowired
	private Shell shell;

	@Bean
	public CompleterAdapter completer() {
		CompleterAdapter completerAdapter = new CompleterAdapter();
		completerAdapter.setShell(shell);
		return completerAdapter;
	}


	public static class CompleterAdapter implements Completer {

		private Shell shell;

		@Override
		public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
			CompletingParsedLine cpl = (line instanceof CompletingParsedLine) ? ((CompletingParsedLine) line) : t -> t;

			CompletionContext context = new CompletionContext(sanitizeInput(line.words()), line.wordIndex(), line.wordCursor());

			List<CompletionProposal> proposals = shell.complete(context);
			proposals.stream()
				.map(p -> new Candidate(
					p.dontQuote() ? p.value() : cpl.emit(p.value()).toString(),
					p.displayText(),
					p.category(),
					p.description(),
					null,
					null,
					true)
				)
				.forEach(candidates::add);
		}

		public void setShell(Shell shell) {
			this.shell = shell;
		}

		static List<String> sanitizeInput(List<String> words) {
			words = words.stream()
				.map(s -> s.replaceAll("^\\n+|\\n+$", "")) // CR at beginning/end of line introduced by backslash continuation
				.map(s -> s.replaceAll("\\n+", " ")) // CR in middle of word introduced by return inside a quoted string
				.collect(Collectors.toList());
			return words;
		}
	}

}
