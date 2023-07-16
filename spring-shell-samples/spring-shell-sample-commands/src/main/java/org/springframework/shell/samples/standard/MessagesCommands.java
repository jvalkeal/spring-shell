/*
 * Copyright 2023 the original author or authors.
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

import java.util.Locale;

import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.shell.message.MessageContext;
import org.springframework.shell.message.ShellMessages;

@Command(command = "messages")
public class MessagesCommands {

	private ShellMessages shellMessages;

	public MessagesCommands(ShellMessages shellMessages) {
		this.shellMessages = shellMessages;
	}

	@Command(command = "helloworld")
	public String helloworld(
		@Option(defaultValue = "en") LocaleEnum language
	) {
		Locale locale = new Locale.Builder().setLanguage(language.toString()).build();
		MessageContext context = new MessageContext(locale);
		Object[] inserts = new Object[] { locale };
		String resolve = shellMessages.resolve(context, "helloWorld", inserts, "Hello World ''{0}''");
		return resolve;
	}

	public enum LocaleEnum {
		en,
		fi,
		de,
		fr,
		ja;
	}

}
