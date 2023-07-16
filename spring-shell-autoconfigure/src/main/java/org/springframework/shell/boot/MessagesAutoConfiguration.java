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
package org.springframework.shell.boot;

import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.shell.message.MessageResolver;
import org.springframework.shell.message.MessageResolverShellMessages;
import org.springframework.shell.message.MessageSourceMessageResolver;
import org.springframework.shell.message.ShellMessages;

/**
 *
 * @author Janne Valkealahti
 */
@AutoConfiguration
public class MessagesAutoConfiguration {

	@Bean
	public MessageResolver shellMessageResolver(MessageSource messageSource) {
		return new MessageSourceMessageResolver(messageSource);
	}

	@Bean
	public ShellMessages shellMessages(List<MessageResolver> resolvers) {
		return new MessageResolverShellMessages(resolvers);
	}
}
