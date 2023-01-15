package org.springframework.shell.command.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

		ParseResultVisitor visitor = new ParseResultVisitor();
		// visitor.visit(commandNode);
	}

	void tokensToNodes(List<Token> tokens) {

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
				if (argument.length() > 2 && argument.charAt(0) == '[' && argument.charAt(1) != ']'
						&& argument.charAt(1) != ':' && argument.charAt(argument.length() - 1) == ']') {
					tokenList.add(Token.of(argument, TokenType.DIRECTIVE, i));
					continue;
				}
			}

			// if (!configuration.RootCommand.HasAlias(arg))
			// {
			// 	foundEndOfDirectives = true;
			// }


			// Set<String> validTokens = commandModel.getValidRootTokens().keySet();
			Map<String, Token> validRootTokens = commandModel.getValidRootTokens();
			if (validRootTokens.containsKey(argument)) {
				Token token = validRootTokens.get(argument);
				switch (token.getType()) {
					case OPTION:
						// tokenList.Add(Option(arg, (Option)token.Symbol!));
						// tokenList.add(Token.of(argument, TokenType.OPTION, i));
						break;
					case COMMAND:
						break;
					default:
						break;
				}
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
