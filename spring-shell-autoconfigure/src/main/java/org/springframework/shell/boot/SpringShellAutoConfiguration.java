/*
 * Copyright 2017-2022 the original author or authors.
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

import java.util.Set;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.shell.ResultHandler;
import org.springframework.shell.ResultHandlerService;
import org.springframework.shell.Shell;
import org.springframework.shell.command.CommandCatalog;
import org.springframework.shell.result.GenericResultHandlerService;
import org.springframework.shell.result.ResultHandlerConfig;

/**
 * Creates supporting beans for running the Shell
 */
@Configuration(proxyBeanMethods = false)
@Import(ResultHandlerConfig.class)
public class SpringShellAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(ConversionService.class)
	public ConversionService shellConversionService(ApplicationContext applicationContext) {
		FormattingConversionService service = new FormattingConversionService();
		DefaultConversionService.addDefaultConverters(service);
		DefaultConversionService.addCollectionConverters(service);
		ApplicationConversionService.addBeans(service, applicationContext);
		return service;
	}

	@Bean
	public ResultHandlerService resultHandlerService(Set<ResultHandler<?>> resultHandlers) {
		GenericResultHandlerService service = new GenericResultHandlerService();
		for (ResultHandler<?> resultHandler : resultHandlers) {
			service.addResultHandler(resultHandler);
		}
		return service;
	}

	@Bean
	public Shell shell(ResultHandlerService resultHandlerService, CommandCatalog commandRegistry) {
		return new Shell(resultHandlerService, commandRegistry);
	}
}
