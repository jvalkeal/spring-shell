package org.springframework.shell.boot;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.messaging.handler.annotation.support.HeaderMethodArgumentResolver;
import org.springframework.messaging.handler.annotation.support.HeadersMethodArgumentResolver;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.shell.ParameterResolver;
import org.springframework.shell.command.CommandContextMethodArgumentResolver;
import org.springframework.shell.command.CommandExecution.CommandExecutionHandlerMethodArgumentResolvers;
import org.springframework.shell.standard.ShellOptionMethodArgumentResolver;
import org.springframework.shell.standard.StandardParameterResolver;
import org.springframework.shell.standard.ValueProvider;

@Configuration(proxyBeanMethods = false)
public class ParameterResolverAutoConfiguration {

	@Bean
	public ParameterResolver standardParameterResolver(ConversionService conversionService,
			ObjectProvider<ValueProvider> valueProviders) {
		Set<ValueProvider> collect = valueProviders.orderedStream().collect(Collectors.toSet());
		return new StandardParameterResolver(conversionService, collect);
	}

	@Bean
	public CommandExecutionHandlerMethodArgumentResolvers commandExecutionHandlerMethodArgumentResolvers() {
		List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();
		resolvers.add(new HeaderMethodArgumentResolver(new DefaultConversionService(), null));
		resolvers.add(new HeadersMethodArgumentResolver());
		resolvers.add(new CommandContextMethodArgumentResolver());
		resolvers.add(new ShellOptionMethodArgumentResolver(new DefaultConversionService(), null));
		return new CommandExecutionHandlerMethodArgumentResolvers(resolvers);
	}
}
