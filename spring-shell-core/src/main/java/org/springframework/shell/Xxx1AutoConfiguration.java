package org.springframework.shell;

import org.jline.reader.LineReader;
import org.jline.reader.Parser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.shell.jline.InteractiveShellApplicationRunner;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.shell.jline.ScriptShellApplicationRunner;

import static org.springframework.shell.jline.InteractiveShellApplicationRunner.SPRING_SHELL_INTERACTIVE;
import static org.springframework.shell.jline.ScriptShellApplicationRunner.SPRING_SHELL_SCRIPT;

@Configuration
public class Xxx1AutoConfiguration {

	@Autowired
	private Shell shell;

	@Autowired
	private PromptProvider promptProvider;

	@Autowired
	private LineReader lineReader;

	@Bean
	@ConditionalOnProperty(prefix = SPRING_SHELL_INTERACTIVE, value = InteractiveShellApplicationRunner.ENABLED, havingValue = "true", matchIfMissing = true)
	public ApplicationRunner interactiveApplicationRunner(Parser parser, Environment environment) {
		return new InteractiveShellApplicationRunner(lineReader, promptProvider, parser, shell, environment);
	}

	@Bean
	@ConditionalOnProperty(prefix = SPRING_SHELL_SCRIPT, value = ScriptShellApplicationRunner.ENABLED, havingValue = "true", matchIfMissing = true)
	public ApplicationRunner scriptApplicationRunner(Parser parser, ConfigurableEnvironment environment) {
		return new ScriptShellApplicationRunner(parser, shell, environment);
	}
}
