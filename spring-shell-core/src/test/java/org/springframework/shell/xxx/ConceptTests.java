package org.springframework.shell.xxx;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConceptTests {

	private Pojo1 pojo1 = new Pojo1();

	@Test
	public void testSimpleFunctionExecution() {
		CommandCatalog catalog = new DefaultCommandCatalog();
		CommandRegistration r1 = CommandRegistration.builder()
			.command("command1")
			.help("help")
			.withOption()
				.name("--arg1")
				.description("some arg1")
				.and()
			.withAction()
				.function(ctx -> "hi")
				.and()
			.build();
		catalog.register(r1);
		CommandExecution execution = new CommandExecution();
		Object result = execution.evaluate(r1, new String[]{"--arg1", "myarg1value"});
		assertThat(result).isEqualTo("hi");
	}

	void xxx2 () {
		CommandRegistration xxx = CommandRegistration.builder()
			.type(String.class)
			.command("group1 sub1")
			.help("help")
			.withOption()
				.and()
			.withAction()
				.function(ctx -> "hi")
				.method(pojo1, "method1")
				.and()
			.build();
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
