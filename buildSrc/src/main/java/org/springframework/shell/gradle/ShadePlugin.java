/*
 * Copyright 2024 the original author or authors.
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
package org.springframework.shell.gradle;

import java.util.Arrays;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.plugins.PluginManager;

import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin;
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar;

public class ShadePlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		PluginManager pluginManager = project.getPluginManager();
		pluginManager.apply(ShadowPlugin.class);

		ConfigurationContainer configurations = project.getConfigurations();
		configurations.create("shade", (shade) -> {
			Configuration management = configurations.getByName(ManagementConfigurationPlugin.MANAGEMENT_CONFIGURATION_NAME);
			shade.extendsFrom(management);

			PluginContainer plugins = project.getPlugins();
			plugins.withType(ShadowPlugin.class, (shadowPlugin) -> {
				Task task = project.getTasks().findByName("shadowJar");
				ShadowJar shadowJarTask = (ShadowJar) task;
				shadowJarTask.setConfigurations(Arrays.asList(shade));
			});
		});

	}

}
