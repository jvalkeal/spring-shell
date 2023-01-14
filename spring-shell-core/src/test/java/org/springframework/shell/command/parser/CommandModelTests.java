package org.springframework.shell.command.parser;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.shell.command.CommandRegistration;

import static org.assertj.core.api.Assertions.assertThat;

class CommandModelTests {

	@Test
	void onePlainRootCommand() {
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

	@Test
	void onePlainSubcCommand() {
		Map<String, CommandRegistration> registrations = new HashMap<>();
		CommandRegistration root1sub1 = CommandRegistration.builder()
			.command("root1", "sub1")
			.withOption()
				.longNames("arg1")
				.and()
			.withTarget()
				.consumer(ctx -> {})
				.and()
			.build();
		registrations.put("root1 sub1", root1sub1);
		CommandModel model = new CommandModel(registrations);
		Map<String, Token> tokens = model.getValidRootTokens();
		assertThat(tokens).hasSize(1);
		assertThat(tokens.get("root1")).satisfies(token -> {
			assertThat(token).isNotNull();
			assertThat(token.getValue()).isEqualTo("root1");
			assertThat(token.getType()).isEqualTo(TokenType.COMMAND);
		});

		assertThat(model.getRootCommand("root1")).satisfies(root1 -> {
			assertThat(root1).isNotNull();
			assertThat(root1.children).hasSize(1);
			assertThat(root1.registration).isNull();
			assertThat(root1.children.get(0)).satisfies(sub1 -> {
				assertThat(sub1).isNotNull();
				assertThat(sub1.children).isEmpty();
				assertThat(sub1.registration).isNotNull();
			});
		});
	}
}
