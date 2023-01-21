/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.shell.command.parser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;

import org.springframework.shell.command.CommandRegistration;
import org.springframework.shell.command.parser.Ast.AstResult;
import org.springframework.shell.command.parser.Lexer.LexerResult;
import org.springframework.shell.command.parser.Parser.ParseResult;

abstract class AbstractParsingTests {

	static final CommandRegistration ROOT1 = CommandRegistration.builder()
		.command("root1")
		.withTarget()
			.consumer(ctx -> {})
			.and()
		.build();

	static final CommandRegistration ROOT2 = CommandRegistration.builder()
		.command("root2")
		.withTarget()
			.consumer(ctx -> {})
			.and()
		.build();

	static final CommandRegistration ROOT2_SUB1 = CommandRegistration.builder()
			.command("root2", "sub1")
			.withOption()
				.longNames("arg1")
				.and()
			.withTarget()
				.consumer(ctx -> {})
				.and()
			.build();

	static final CommandRegistration ROOT3 = CommandRegistration.builder()
		.command("root3")
		.withOption()
			.longNames("arg1")
			.and()
		.withTarget()
			.consumer(ctx -> {})
			.and()
		.build();

	static final CommandRegistration ROOT4 = CommandRegistration.builder()
		.command("root4")
		.withOption()
			.longNames("arg1")
			.required()
			.and()
		.withTarget()
			.consumer(ctx -> {})
			.and()
		.build();

	static final CommandRegistration ROOT5 = CommandRegistration.builder()
		.command("root5")
		.withOption()
			.longNames("arg1")
			.required()
			.and()
		.withOption()
			.longNames("arg2")
			.required()
			.and()
		.withTarget()
			.consumer(ctx -> {})
			.and()
		.build();

	static final CommandRegistration ROOT6_OPTION_INT = CommandRegistration.builder()
		.command("root6")
		.withOption()
			.longNames("arg1")
			.type(int.class)
			.required()
			.and()
		.withTarget()
			.consumer(ctx -> {})
			.and()
		.build();

	Map<String, CommandRegistration> registrations = new HashMap<>();

	@BeforeEach
	void setup() {
		registrations.clear();
	}

	void register(CommandRegistration registration) {
		registrations.put(registration.getCommand(), registration);
	}

	CommandModel commandModel() {
		return new CommandModel(registrations);
	}


	Token token(String value, TokenType type, int position) {
		return new Token(value, type, position);
	}

	Lexer lexer() {
		return new Lexer.DefaultLexer(commandModel(), new ParserConfiguration());
	}

	Lexer lexer(ParserConfiguration configuration) {
		return new Lexer.DefaultLexer(commandModel(), configuration);
	}

	Lexer lexer(CommandModel commandModel) {
		return new Lexer.DefaultLexer(commandModel, new ParserConfiguration());
	}

	Lexer lexer(CommandModel commandModel, ParserConfiguration configuration) {
		return new Lexer.DefaultLexer(commandModel, configuration);
	}

	List<Token> tokenize(String... arguments) {
		return tokenize(lexer(), arguments);
	}

	List<Token> tokenize(Lexer lexer, String... arguments) {
		return lexer.tokenize(Arrays.asList(arguments)).tokens();
	}

	LexerResult tokenizeAsResult(String... arguments) {
		return tokenizeAsResult(lexer(), arguments);
	}

	LexerResult tokenizeAsResult(Lexer lexer, String... arguments) {
		return lexer.tokenize(Arrays.asList(arguments));
	}

	Ast ast() {
		return new Ast.DefaultAst();
	}

	AstResult ast(List<Token> tokens) {
		Ast ast = ast();
		return ast.generate(tokens);
	}

	AstResult ast(Token... tokens) {
		Ast ast = ast();
		return ast.generate(Arrays.asList(tokens));
	}

	ParseResult parse(String... arguments) {
		CommandModel commandModel = commandModel();
		Lexer lexer = lexer(commandModel);
		Ast ast = ast();
		Parser parser = new Parser.DefaultParser(commandModel, lexer, ast);
		return parser.parse(Arrays.asList(arguments));
	}

	ParseResult parse(Lexer lexer, String... arguments) {
		CommandModel commandModel = commandModel();
		Ast ast = ast();
		Parser parser = new Parser.DefaultParser(commandModel, lexer, ast);
		return parser.parse(Arrays.asList(arguments));
	}
}
