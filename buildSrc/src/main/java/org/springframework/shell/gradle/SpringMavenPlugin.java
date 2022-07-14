package org.springframework.shell.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;

public class SpringMavenPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		PluginManager pluginManager = project.getPluginManager();
		pluginManager.apply(MavenPublishPlugin.class);
		pluginManager.apply(PublishLocalPlugin.class);
		pluginManager.apply(PublishAllJavaComponentsPlugin.class);

		project.getPlugins().withType(MavenPublishPlugin.class).all((mavenPublish) -> {
				PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);
				if (project.hasProperty("deploymentRepository")) {
						publishing.getRepositories().maven((mavenRepository) -> {
								mavenRepository.setUrl(project.property("deploymentRepository"));
								mavenRepository.setName("deployment");
						});
				}
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
