package org.springframework.shell.xxx;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import org.springframework.shell.command.CommandExecution.CommandParserExceptionsException;

import java.util.Optional;

public class CommandParserExceptionResolver implements ExceptionResolver {

	@Override
	public Optional<HandlingResult> resolve(Throwable throwable) {

		// Missing mandatory option "--name", Catalog name.
		// Use the command "help catalog add" for additional information on command options

		if (throwable instanceof CommandParserExceptionsException ex) {
			AttributedStringBuilder builder = new AttributedStringBuilder();
			builder.append("XXXXXX\n");
			ex.getParserExceptions().stream().forEach(e -> {
				builder.append(new AttributedString(e.getMessage(), AttributedStyle.DEFAULT.foreground(AttributedStyle.RED)));
				builder.append("\n");
			});
			AttributedString as = builder.toAttributedString();
			HandlingResult handlingResult = new HandlingResult() {

				@Override
				public AttributedString getMessage() {
					return as;
				}
			};
			return Optional.of(handlingResult);
		}

		return null;
	}

}
