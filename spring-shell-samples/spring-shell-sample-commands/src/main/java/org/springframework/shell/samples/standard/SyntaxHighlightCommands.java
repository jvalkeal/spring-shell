/*
 * Copyright 2024 the original author or authors.
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
package org.springframework.shell.samples.standard;

import org.eclipse.tm4e.core.TMException;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.grammar.IStateStack;
import org.eclipse.tm4e.core.grammar.IToken;
import org.eclipse.tm4e.core.grammar.ITokenizeLineResult;
import org.eclipse.tm4e.core.registry.Registry;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.standard.AbstractShellComponent;

import static org.eclipse.tm4e.core.registry.IGrammarSource.fromResource;

@Command
public class SyntaxHighlightCommands extends AbstractShellComponent {

	private static final String JAVA1 = """
			package com.example.demo;

			import org.springframework.boot.SpringApplication;
			import org.springframework.boot.autoconfigure.SpringBootApplication;

			@SpringBootApplication
			public class DemoApplication {

				public static void main(String[] args) {
					SpringApplication.run(DemoApplication.class, args);
				}

			}
			""";

	private static final String JAVA2 = """
			package com.example.demo;
			""";

	@Command(command = "syntaxhighlight java1")
	public String java1() {
		StringBuilder builder = new StringBuilder();
		Registry registry = new Registry();
		final IGrammar grammar = registry.addGrammar(fromResource(TMException.class, "/syntax/java/java.tmLanguage.json"));
		String[] lines = JAVA1.split(System.lineSeparator());
		IStateStack ruleStack = null;
		int i = 0;
		for (final String line : lines) {
			// final var lineTokens = grammar.tokenizeLine(line, ruleStack, null);
			ITokenizeLineResult<IToken[]> lineTokens = grammar.tokenizeLine(line, ruleStack, null);

			String convert = convert(line, lineTokens);
			builder.append(convert);
			builder.append(System.lineSeparator());

			// ruleStack = lineTokens.getRuleStack();
			// for (i = 0; i < lineTokens.getTokens().length; i++) {
			// 	final IToken token = lineTokens.getTokens()[i];
			// 	final String s = "Token from " + token.getStartIndex() + " to " + token.getEndIndex() + " with scopes "
			// 			+ token.getScopes();
			// 	builder.append(s);
			// 	builder.append(System.lineSeparator());
			// }

		}

		return builder.toString();
	}

	private String convert(String line, ITokenizeLineResult<IToken[]> lineTokens) {
		StringBuilder builder = new StringBuilder();
		int i = 0;
		for (i = 0; i < lineTokens.getTokens().length; i++) {
			final IToken token = lineTokens.getTokens()[i];
			CharSequence sub = line.subSequence(token.getStartIndex(), Math.min(token.getEndIndex(), line.length()));
			if (token.getScopes().contains("keyword.other.package.java")) {
				AttributedStringBuilder sb = new AttributedStringBuilder();
				sb.style(sb.style().foreground(AttributedStyle.BLUE));
				sb.append(sub);
				builder.append(sb.toAnsi());
			}
			else if (token.getScopes().contains("keyword.other.import.java")) {
				AttributedStringBuilder sb = new AttributedStringBuilder();
				sb.style(sb.style().foreground(AttributedStyle.BLUE));
				sb.append(sub);
				builder.append(sb.toAnsi());
			}
			else if (token.getScopes().contains("punctuation.definition.annotation.java")) {
				AttributedStringBuilder sb = new AttributedStringBuilder();
				sb.style(sb.style().foreground(AttributedStyle.BLUE));
				sb.append(sub);
				builder.append(sb.toAnsi());
			}
			else if (token.getScopes().contains("storage.type.annotation.java")) {
				AttributedStringBuilder sb = new AttributedStringBuilder();
				sb.style(sb.style().foreground(AttributedStyle.BLUE));
				sb.append(sub);
				builder.append(sb.toAnsi());
			}
			else if (token.getScopes().contains("storage.modifier.java")) {
				AttributedStringBuilder sb = new AttributedStringBuilder();
				sb.style(sb.style().foreground(AttributedStyle.GREEN));
				sb.append(sub);
				builder.append(sb.toAnsi());
			}
			else if (token.getScopes().contains("storage.modifier.java")) {
				AttributedStringBuilder sb = new AttributedStringBuilder();
				sb.style(sb.style().foreground(AttributedStyle.GREEN));
				sb.append(sub);
				builder.append(sb.toAnsi());
			}
			else if (token.getScopes().contains("variable.other.object.property.java")) {
				AttributedStringBuilder sb = new AttributedStringBuilder();
				sb.style(sb.style().foreground(AttributedStyle.GREEN));
				sb.append(sub);
				builder.append(sb.toAnsi());
			}
			else {
				builder.append(sub);
			}
		}
		return builder.toString();
	}

}
