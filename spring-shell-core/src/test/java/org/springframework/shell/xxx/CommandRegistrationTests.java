package org.springframework.shell.xxx;

public class CommandRegistrationTests {

	void xxx1 () {
		CommandRegistration<String> xxx = CommandRegistration.<String>builder()
			.command("command1")
			.help("help")
			.withOption()
				.and()
			.withAction()
				.function(ctx -> "hi")
				.and()
			.build();
	}

	void xxx2 () {
		CommandRegistration<String> xxx = CommandRegistration.<String>builder()
			.command("group1 sub1")
			.help("help")
			.withOption()
				.and()
			.withAction()
				.function(ctx -> "hi")
				.and()
			.build();
	}

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
