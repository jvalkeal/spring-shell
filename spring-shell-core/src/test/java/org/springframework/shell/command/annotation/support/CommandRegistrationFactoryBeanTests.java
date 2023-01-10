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
package org.springframework.shell.command.annotation.support;

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.shell.command.CommandRegistration;
import org.springframework.shell.command.annotation.Command;

import static org.assertj.core.api.Assertions.assertThat;

class CommandRegistrationFactoryBeanTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();
	private final static String BEAN = "commandBean";
	private final static String FACTORYBEAN = "commandRegistrationFactoryBean";
	private final static String FACTORYBEANREF = "&" + FACTORYBEAN;

	@Test
	void hiddenOnClassLevel() {
		HiddenOnClassBean commands = new HiddenOnClassBean();
		this.contextRunner
				.withBean(BEAN, HiddenOnClassBean.class, () -> commands)
				.withBean(FACTORYBEAN, CommandRegistrationFactoryBean.class, () -> new CommandRegistrationFactoryBean(), bd -> {
					bd.getPropertyValues().add(CommandRegistrationFactoryBean.COMMAND_BEAN_NAME, BEAN);
					bd.getPropertyValues().add(CommandRegistrationFactoryBean.COMMAND_METHOD_NAME, "command");
					bd.getPropertyValues().add(CommandRegistrationFactoryBean.COMMAND_METHOD_PARAMETERS, new Class[0]);
				})
				.run((context) -> {
					CommandRegistrationFactoryBean fb = context.getBean(FACTORYBEANREF,
							CommandRegistrationFactoryBean.class);
					assertThat(fb).isNotNull();
					CommandRegistration registration = fb.getObject();
					assertThat(registration).isNotNull();
					assertThat(registration.isHidden()).isTrue();
				});
	}

	@Command(hidden = true)
	private static class HiddenOnClassBean {

		@Command
		void command(){
		}
	}

}
