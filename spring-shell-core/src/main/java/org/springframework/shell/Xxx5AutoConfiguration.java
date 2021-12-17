package org.springframework.shell;

import java.nio.file.Paths;

import org.jline.reader.LineReader;
import org.jline.reader.impl.history.DefaultHistory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Xxx5AutoConfiguration {

	@Configuration
	@ConditionalOnMissingBean(org.jline.reader.History.class)
	public static class JLineHistoryConfiguration {

		// @Bean
		// public org.jline.reader.History history(LineReader lineReader, @Value("${spring.application.name:spring-shell}.log") String historyPath) {
		// 	lineReader.setVariable(LineReader.HISTORY_FILE, Paths.get(historyPath));
		// 	return new DefaultHistory(lineReader);
		// }

		@Bean
		public org.jline.reader.History history(@Value("${spring.application.name:spring-shell}.log") String historyPath) {
			return new DefaultHistory();
		}

	}
}
