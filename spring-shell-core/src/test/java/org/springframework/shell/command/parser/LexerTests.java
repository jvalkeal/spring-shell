package org.springframework.shell.command.parser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.shell.command.CommandRegistration;

import static org.assertj.core.api.Assertions.assertThat;

class LexerTests {

	@Test
	void tokenizeOnePlainRoot() {
		Map<String, CommandRegistration> registrations = new HashMap<>();

		CommandRegistration root1 = CommandRegistration.builder()
			.command("root1")
			.withTarget()
				.consumer(ctx -> {})
				.and()
			.build();
		registrations.put("root1", root1);

		CommandRegistration root2sub1 = CommandRegistration.builder()
			.command("root2", "sub1")
			.withOption()
				.longNames("arg1")
				.and()
			.withTarget()
				.consumer(ctx -> {})
				.and()
			.build();

		registrations.put("root1", root1);
		registrations.put("root2 sub1", root2sub1);

		CommandModel model = new CommandModel(registrations);

		Lexer lexer = new Lexer.DefaultLexer(model, new ParserConfiguration());
		List<Token> tokens = lexer.tokenize(Arrays.asList("root1"));

		assertThat(tokens).hasSize(2);
		assertThat(tokens.get(0)).satisfies(token -> {
			assertThat(token).isNotNull();
			assertThat(token.getType()).isEqualTo(TokenType.COMMAND);
			assertThat(token.getValue()).isEqualTo("root1");
		});
	}
}
