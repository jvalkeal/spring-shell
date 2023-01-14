package org.springframework.shell.command.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.springframework.shell.command.parser.CommandModel.CommandInfo;

public class Parser {

	private final ParserConfiguration configuration;
	CommandModel commandModel;

	Parser(CommandModel commandModel) {
		this(commandModel, new ParserConfiguration());
	}

	Parser(CommandModel commandModel, ParserConfiguration configuration) {
		this.commandModel = commandModel;
		this.configuration = configuration;
	}

	// [ root1 [ sub1 [ --arg1 <asdf> ] ] ]
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
		var foundEndOfDirectives = !configuration.isEnableDirectives();

		List<Token> tokenList = new ArrayList<Token>(arguments.size());

		int i = -1;
		for (String argument : arguments) {
			i++;

			if (foundDoubleDash) {
				tokenList.add(Token.of(argument, TokenType.ARGUMENT, i));
				continue;
			}

			if (!foundDoubleDash && "--".equals(argument)) {
					tokenList.add(Token.of(argument, TokenType.DOUBLEDASH, i));
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


	// Token Argument(string value) => new(value, TokenType.Argument, default, i);
	// Token CommandArgument(string value, Command command) => new(value, TokenType.Argument, command, i);
	// Token OptionArgument(string value, Option option) => new(value, TokenType.Argument, option, i);
	// Token Command(string value, Command cmd) => new(value, TokenType.Command, cmd, i);
	// Token Option(string value, Option option) => new(value, TokenType.Option, option, i);
	// Token DoubleDash() => new("--", TokenType.DoubleDash, default, i);
	// Token Directive(string value) => new(value, TokenType.Directive, default, i);
}
