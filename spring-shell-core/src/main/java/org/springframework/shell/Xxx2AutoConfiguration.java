package org.springframework.shell;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class Xxx2AutoConfiguration {

	@Bean
	public CommandRegistry commandRegistry(
			ObjectProvider<MethodTargetRegistrar> methodTargerRegistrars) {
		ConfigurableCommandRegistry registry = new ConfigurableCommandRegistry();
		methodTargerRegistrars.orderedStream().forEach(resolver -> {
			resolver.register(registry);
		});
		return registry;
	}
}
