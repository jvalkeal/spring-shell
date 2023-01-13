package org.springframework.shell.command.parser;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.shell.command.CommandRegistration;

import static org.assertj.core.api.Assertions.assertThat;

class CommandModelTests {

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
		Map<String, Token> tokens = model.getValidRootTokens();
		assertThat(tokens).hasSize(1);
		assertThat(tokens.get("root1")).satisfies(token -> {
			assertThat(token).isNotNull();
			assertThat(token.getValue()).isEqualTo("root1");
			assertThat(token.getType()).isEqualTo(TokenType.COMMAND);
		});
	}
}
