package org.springframework.shell.command.parser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.shell.command.CommandRegistration;

import static org.assertj.core.api.Assertions.assertThat;

class ParserTests {

	@Test
	void test() {
		Map<String, CommandRegistration> registrations = new HashMap<>();
		CommandRegistration root1 = CommandRegistration.builder()
			.command("root1")
			.withTarget()
				.consumer(ctx -> {})
				.and()
			.build();
		registrations.put("root1", root1);
		CommandModel model = new CommandModel(registrations);

		Parser parser = new Parser(model);
		List<Token> tokens = parser.tokenize(Arrays.asList("root1"));
		assertThat(tokens).hasSize(1);
		assertThat(tokens.get(0)).satisfies(token -> {
			assertThat(token).isNotNull();
			assertThat(token.getType()).isEqualTo(TokenType.COMMAND);
			assertThat(token.getValue()).isEqualTo("root1");
		});
	}
}
