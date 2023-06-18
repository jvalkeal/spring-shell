package org.springframework.shell.result;

import org.jline.terminal.Terminal;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.shell.CommandNotFound;
import org.springframework.shell.ResultHandler;
import org.springframework.shell.result.CommandNotFoundMessageProvider.ProviderContext;
import org.springframework.util.Assert;

/**
 * {@link ResultHandler} for {@link CommandNotFound} using
 * {@link CommandNotFoundMessageProvider} to provide an error message.
 *
 * @author Janne Valkealahti
 */
public final class CommandNotFoundResultHandler extends TerminalAwareResultHandler<CommandNotFound> {

	private CommandNotFoundMessageProvider provider;

	public CommandNotFoundResultHandler(Terminal terminal, ObjectProvider<CommandNotFoundMessageProvider> provider) {
		super(terminal);
		Assert.notNull(provider, "provider cannot be null");
		this.provider = provider.getIfAvailable(() -> new DefaultProvider());
	}

	@Override
	protected void doHandleResult(CommandNotFound result) {
		ProviderContext context = CommandNotFoundMessageProvider.contextOf(result.getRegistrations(), result.getText());
		String message = provider.apply(context);
		terminal.writer().println(message);
		terminal.writer().flush();
	}

	private static class DefaultProvider implements CommandNotFoundMessageProvider {

		@Override
		public String apply(ProviderContext context) {
			return String.format("Command not found for '%s'", context.text());
		}

	}
}
