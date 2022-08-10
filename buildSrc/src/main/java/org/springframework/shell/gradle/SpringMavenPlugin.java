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
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;

public class SpringMavenPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		PluginManager pluginManager = project.getPluginManager();
		pluginManager.apply(MavenPublishPlugin.class);
		pluginManager.apply(PublishLocalPlugin.class);
		pluginManager.apply(PublishAllJavaComponentsPlugin.class);

		project.getPlugins().withType(MavenPublishPlugin.class).all((mavenPublish) -> {
				// PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);
				// if (project.hasProperty("deploymentRepository")) {
				// 		publishing.getRepositories().maven((mavenRepository) -> {
				// 				mavenRepository.setUrl(project.property("deploymentRepository"));
				// 				mavenRepository.setName("deployment");
				// 		});
				// }
				// publishing.getPublications().withType(MavenPublication.class)
				// 				.all((mavenPublication) -> customizeMavenPublication(mavenPublication, project));
				project.getPlugins().withType(JavaPlugin.class).all((javaPlugin) -> {
						JavaPluginExtension extension = project.getExtensions().getByType(JavaPluginExtension.class);
						extension.withJavadocJar();
						extension.withSourcesJar();
				});
		});

	}
}
