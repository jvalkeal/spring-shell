package org.springframework.shell.xxx;

import java.util.function.Function;

public class CommandRegistration<T> {

	String command;
	String help;
	String description;
	Function<CommandExecutionContext, T> function;

	public static <T> Builder<T> builder() {
		return null;
	}

	public interface OptionSpec<T> {
		Builder<T> and();
	}

	public interface ActionSpec<T> {
		ActionSpec<T> function(Function<CommandExecutionContext, T> function);
		Builder<T> and();
	}

	public interface Builder<T> {
		Builder<T> command(String help);
		Builder<T> help(String help);
		OptionSpec<T> withOption();
		ActionSpec<T> withAction();
		CommandRegistration<T> build();
	}



}
