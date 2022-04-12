/*
 * Copyright 2022 the original author or authors.
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

import org.springframework.shell.command.CommandRegistration;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class RegisterCommands extends AbstractShellComponent {

	private final PojoMethods pojoMethods = new PojoMethods();
	private final CommandRegistration dynamic1;
	private final CommandRegistration dynamic2;
	private final CommandRegistration dynamic3;

	public RegisterCommands() {
		dynamic1 = CommandRegistration.builder().command("dynamic1").targetMethod().method(pojoMethods, "dynamic1").and().build();
		dynamic2 = CommandRegistration.builder().command("dynamic2").targetMethod().method(pojoMethods, "dynamic2").and().build();
		dynamic3 = CommandRegistration.builder().command("dynamic3").targetMethod().method(pojoMethods, "dynamic3").and().build();
	}

    @ShellMethod(key = "register add", value = "Register commands", group = "Register Commands")
    public String register() {
		getCommandRegistry().register(dynamic1);
		getCommandRegistry().register(dynamic2);
		getCommandRegistry().register(dynamic3);
		return "Registered commands dynamic1, dynamic2, dynamic3";
    }

    @ShellMethod(key = "register remove", value = "Deregister commands", group = "Register Commands")
    public String deregister() {
		getCommandRegistry().unregister(dynamic1);
		getCommandRegistry().unregister(dynamic2);
		getCommandRegistry().unregister(dynamic3);
		return "Deregistered commands dynamic1, dynamic2, dynamic3";
    }

	public static class PojoMethods {

		@ShellMethod
		public String dynamic1() {
			return "dynamic1";
		}

		@ShellMethod
		public String dynamic2(String arg1) {
			return "dynamic2" + arg1;
		}

		@ShellMethod
		public String dynamic3(@ShellOption(defaultValue = ShellOption.NULL) String arg1) {
			return "dynamic3" + arg1;
		}
	}
}
