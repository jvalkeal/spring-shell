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
package org.springframework.shell.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.api.tasks.testing.Test;
import org.gradle.external.javadoc.CoreJavadocOptions;

/**
 * @author Janne Valkealahti
 */
class ModulePlugin implements Plugin<Project> {

	@Override
	public final void apply(Project project) {
		PluginManager pluginManager = project.getPluginManager();
		pluginManager.apply(JavaPlugin.class);
		pluginManager.apply(ManagementConfigurationPlugin.class);
		pluginManager.apply(JavaLibraryPlugin.class);
		pluginManager.apply(SpringMavenPlugin.class);

		project.getPlugins().withType(JavaBasePlugin.class, java -> {

			project.getTasks().withType(Javadoc.class, (javadoc) -> {
				CoreJavadocOptions options = (CoreJavadocOptions) javadoc.getOptions();
				options.source("17");
				options.encoding("UTF-8");
				options.addStringOption("Xdoclint:none", "-quiet");
			});

			project.getTasks().withType(Test.class, test -> {
				test.useJUnitPlatform();
			});

		});

	}
}
