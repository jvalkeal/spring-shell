package org.springframework.shell.gradle;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaLibraryPlugin;
// import org.gradle.api.plugins.MavenPlugin;
import org.gradle.api.plugins.PluginManager;
// import org.springframework.gradle.classpath.CheckClasspathForProhibitedDependenciesPlugin;
// import org.springframework.gradle.maven.SpringMavenPlugin;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.CoreJavadocOptions;

/**
 * @author Janne Valkealahti
 */
class SpringModulePlugin extends AbstractSpringJavaPlugin {

	@Override
	protected void additionalPlugins(Project project) {
		PluginManager pluginManager = project.getPluginManager();
		pluginManager.apply(JavaLibraryPlugin.class);
		pluginManager.apply(SpringMavenPlugin.class);
		// pluginManager.apply(CheckClasspathForProhibitedDependenciesPlugin.class);
		// pluginManager.apply("io.spring.convention.jacoco");

		// def deployArtifacts = project.task("deployArtifacts")
		// deployArtifacts.group = 'Deploy tasks'
		// deployArtifacts.description = "Deploys the artifacts to either Artifactory or Maven Central"
		// if (!Utils.isRelease(project)) {
		// 	deployArtifacts.dependsOn project.tasks.artifactoryPublish
		// }

		project.getPlugins().withType(JavaBasePlugin.class, java -> {

			project.getTasks().withType(Javadoc.class, (javadoc) -> {
				CoreJavadocOptions options = (CoreJavadocOptions) javadoc.getOptions();
				options.source("17");
				options.encoding("UTF-8");
				options.addStringOption("Xdoclint:none", "-quiet");
			});

		});
	}

}
