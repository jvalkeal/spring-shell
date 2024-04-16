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
package org.springframework.shell.samples.catalog;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.sun.jna.Pointer;
import org.jline.terminal.Terminal;
import org.jline.terminal.impl.jna.win.JnaWinSysTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.command.annotation.CommandScan;
import org.springframework.shell.style.FigureSettings;
import org.springframework.shell.style.StyleSettings;
import org.springframework.shell.style.Theme;
import org.springframework.shell.style.ThemeSettings;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

@Configuration
@CommandScan
class CatalogConfiguration {

	private final static Logger log = LoggerFactory.getLogger(CatalogConfiguration.class);

	@Bean
	public TerminalPostProcessor terminalPostProcessor() {
		return new TerminalPostProcessor();
	}


	static class TerminalPostProcessor implements BeanPostProcessor {

		@Override
		public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
			if (beanName.equals("terminal")) {
				if (bean instanceof JnaWinSysTerminal t) {
					log.info("XXX {}", t);
					Field field = ReflectionUtils.findField(JnaWinSysTerminal.class, "consoleIn");
					ReflectionUtils.makeAccessible(field);
					Pointer consoleIn = (Pointer) ReflectionUtils.getField(field, t);

					Method setConsoleMode = ReflectionUtils.findMethod(JnaWinSysTerminal.class, "setConsoleMode", Pointer.class, int.class);
					ReflectionUtils.makeAccessible(setConsoleMode);

					ReflectionUtils.invokeMethod(setConsoleMode, t, consoleIn, 128);
				}
			}
			return bean;
		}
	}

	@Bean
	public Theme customTheme() {
		return new Theme() {
			@Override
			public String getName() {
				return "custom";
			}
			@Override
			public ThemeSettings getSettings() {
				return new CustomThemeSettings();
			}
		};
	}

	static class CustomThemeSettings extends ThemeSettings {
		CustomThemeSettings() {
			super(new CustomStyleSettings(), FigureSettings.defaults());
		}
	}

	static class CustomStyleSettings extends StyleSettings {

		@Override
		public String highlight() {
			return "bold,italic,fg:bright-cyan";
		}

		@Override
		public String background() {
			return "bg:blue";
		}

		@Override
		public String dialogBackground() {
			return "bg:green";
		}

		@Override
		public String buttonBackground() {
			return "bg:red";
		}

		@Override
		public String menubarBackground() {
			return "bg:magenta";
		}

		@Override
		public String statusbarBackground() {
			return "bg:cyan";
		}

	}

}
