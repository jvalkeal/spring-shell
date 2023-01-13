package org.springframework.shell.command.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.springframework.shell.command.parser.CommandModel.CommandInfo;

public class Parser {

	CommandModel commandModel;

	Parser(CommandModel commandModel) {
		this.commandModel = commandModel;
	}

	void parse(List<String> arguments) {
		List<Token> tokens = tokenize(arguments);
		for (Token token : tokens) {
			if (token.getType() == TokenType.COMMAND) {
				new CommandNode(token, token.getValue());
			}
		}

		// new CommandNode(Token, null)
	}

	List<Token> tokenize(List<String> arguments) {

		var foundDoubleDash = false;
		// var foundEndOfDirectives = !configuration.EnableDirectives;
		var foundEndOfDirectives = false;

		List<Token> tokenList = new ArrayList<Token>(arguments.size());

		int i = -1;
		for (String argument : arguments) {
			i++;

			if (foundDoubleDash) {
				tokenList.add(new Token(argument, TokenType.ARGUMENT, i));
				continue;
			}

			if (!foundDoubleDash && "--".equals(argument)) {
					tokenList.add(new Token(argument, TokenType.DOUBLEDASH, i));
					foundDoubleDash = true;
					continue;
			}

			// finding a command marks no-more-directives as those
			// are always before commands.
			if (!foundEndOfDirectives) {

			}

			CommandInfo info = commandModel.getRootCommand(argument);
			tokenList.addAll(commandModel.getValidRootTokens().values());
		}

		return tokenList;
	}

	// Function<String, String> xxx = t -> t;

	// Token Argument(string value) => new(value, TokenType.Argument, default, i);
	// Token CommandArgument(string value, Command command) => new(value, TokenType.Argument, command, i);
	// Token OptionArgument(string value, Option option) => new(value, TokenType.Argument, option, i);
	// Token Command(string value, Command cmd) => new(value, TokenType.Command, cmd, i);
	// Token Option(string value, Option option) => new(value, TokenType.Option, option, i);
	// Token DoubleDash() => new("--", TokenType.DoubleDash, default, i);
	// Token Directive(string value) => new(value, TokenType.Directive, default, i);
}
