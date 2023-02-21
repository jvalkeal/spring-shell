package org.springframework.shell.component.flow;

import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.terminal.Terminal;

import org.springframework.shell.component.context.BaseComponentContext;
import org.springframework.shell.component.context.ComponentContext;
import org.springframework.shell.component.support.AbstractComponent;

/**
 * Flow component having basic "hello world" feature.
 */
public class Test1Component extends AbstractComponent<Test1Component.Test1ComponentContext>{

	private Test1ComponentContext currentContext;

	public Test1Component(Terminal terminal) {
		super(terminal);
	}

	@Override
	public Test1ComponentContext getThisContext(ComponentContext<?> context) {
		if (context != null && currentContext == context) {
			return currentContext;
		}
		currentContext = new DefaultTest1ComponentContext();
		return currentContext;
	}

	@Override
	protected boolean read(BindingReader bindingReader, KeyMap<String> keyMap, Test1ComponentContext context) {
		String operation = bindingReader.readBinding(keyMap);
		if (operation == null) {
			return true;
		}
		switch (operation) {
			case OPERATION_EXIT:
				return true;
			default:
				break;
		}
		return false;
	}

	@Override
	protected Test1ComponentContext runInternal(Test1ComponentContext context) {
		return null;
	}

	@Override
	protected void bindKeyMap(KeyMap<String> keyMap) {
	}

	public interface Test1ComponentSpec extends BaseInputSpec<Test1ComponentSpec> {
	}

	public interface Test1ComponentContext extends ComponentContext<Test1ComponentContext> {
	}

	private static class DefaultTest1ComponentContext extends BaseComponentContext<Test1ComponentContext>
			implements Test1ComponentContext {
	}

}
