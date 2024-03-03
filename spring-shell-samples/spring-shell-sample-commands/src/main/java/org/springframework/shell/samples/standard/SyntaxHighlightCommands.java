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

import java.util.HashMap;
import java.util.Map;

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
			/*
			 * Copyright 2023 the original author or authors.
			 *
			 */
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

	@Command(command = "syntaxhighlight java1")
	public String java1() {
		StringBuilder builder = new StringBuilder();
		Registry registry = new Registry();
		final IGrammar grammar = registry.addGrammar(fromResource(TMException.class, "/syntax/java/java.tmLanguage.json"));
		String[] lines = JAVA1.split(System.lineSeparator());
		// int i = 0;
		IStateStack ruleStack = null;
		for (final String line : lines) {
			ITokenizeLineResult<IToken[]> lineTokens = grammar.tokenizeLine(line, ruleStack, null);
			String convert = convert(line, lineTokens);
			builder.append(convert);
			builder.append(System.lineSeparator());
			// for (int i = 0; i < lineTokens.getTokens().length; i++) {
			// 	final IToken token = lineTokens.getTokens()[i];
			// 	final String s = "Token from " + token.getStartIndex() + " to " + token.getEndIndex() + " with scopes " + token.getScopes();
			// 	builder.append(s);
			// 	builder.append(System.lineSeparator());
			// }
		}

		return builder.toString();
	}

	private final static Map<String, Integer> styleMap = new HashMap<>();

	static {
		styleMap.put("keyword.other.package.java", AttributedStyle.BLUE);
		styleMap.put("storage.modifier.package.java", AttributedStyle.GREEN);
		styleMap.put("keyword.other.import.java", AttributedStyle.BLUE);
		styleMap.put("storage.modifier.import.java", AttributedStyle.GREEN);
		styleMap.put("storage.type.annotation.java", AttributedStyle.GREEN);
		styleMap.put("storage.modifier.java", AttributedStyle.BLUE);
		styleMap.put("entity.name.type.class.java", AttributedStyle.GREEN);
		styleMap.put("variable.other.object.property.java", AttributedStyle.GREEN);
		styleMap.put("storage.type.primitive.java", AttributedStyle.GREEN);
		styleMap.put("punctuation.definition.parameters.begin.bracket.round.java", AttributedStyle.RED);
		styleMap.put("punctuation.definition.parameters.end.bracket.round.java", AttributedStyle.RED);
		styleMap.put("punctuation.bracket.square.java", AttributedStyle.BLUE);
		styleMap.put("storage.type.object.array.java", AttributedStyle.GREEN);
		styleMap.put("entity.name.function.java", AttributedStyle.YELLOW);
		styleMap.put("variable.other.object.java", AttributedStyle.GREEN);
	}

	private String convert(String line, ITokenizeLineResult<IToken[]> lineTokens) {
		StringBuilder builder = new StringBuilder();
		int i = 0;
		for (i = 0; i < lineTokens.getTokens().length; i++) {
			final IToken token = lineTokens.getTokens()[i];
			CharSequence sub = line.subSequence(token.getStartIndex(), Math.min(token.getEndIndex(), line.length()));
			Integer style = null;
			for (String scope : token.getScopes()) {
				Integer s = styleMap.get(scope);
				if (s != null) {
					style = s;
					break;
				}
			}
			if (style != null) {
				AttributedStringBuilder sb = new AttributedStringBuilder();
				sb.style(sb.style().foreground(style));
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
