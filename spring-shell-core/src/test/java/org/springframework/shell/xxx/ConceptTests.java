package org.springframework.shell.xxx;

import java.util.function.Function;

import org.junit.jupiter.api.Test;

import org.springframework.core.ResolvableType;

import static org.assertj.core.api.Assertions.assertThat;

public class ConceptTests {

	private Pojo1 pojo1 = new Pojo1();
	private Function<CommandExecutionContext, String> function1 = ctx -> {
		String arg1 = ctx.getOptionValue("--arg1");
		return "hi" + arg1;
	};

	@Test
	public void testSimpleFunctionExecution() {
		CommandRegistration r1 = CommandRegistration.builder()
			.command("command1")
			.help("help")
			.withOption()
				.name("--arg1")
				.type(ResolvableType.forClass(String.class))
				.description("some arg1")
				.and()
			.withAction()
				.function(function1)
				.and()
			.build();
		CommandExecution execution = CommandExecution.of();
		Object result = execution.evaluate(r1, new String[]{"--arg1", "myarg1value"});
		assertThat(result).isEqualTo("himyarg1value");
	}

	@Test
	public void testCommandCatalog () {
		CommandRegistration r1 = CommandRegistration.builder()
			.command("group1 sub1")
			.build();
		CommandCatalog catalog = CommandCatalog.of();
		catalog.register(r1);
		assertThat(catalog.getCommands()).hasSize(1);
	}

	private static class Pojo1 {

		public void method1() {
		}

		public String method2() {
			return null;
		}

		public void method3(String arg1) {
		}

		public String method4(String arg1) {
			return null;
		}
	}
}
