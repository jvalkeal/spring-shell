package org.springframework.shell.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.PluginManager;
// import org.gradle.plugins.ide.eclipse.EclipseWtpPlugin;
// import org.gradle.plugins.ide.idea.IdeaPlugin;
// import org.springframework.gradle.CopyPropertiesPlugin;
// import org.springframework.gradle.propdeps.PropDepsEclipsePlugin;
// import org.springframework.gradle.propdeps.PropDepsIdeaPlugin;
// import org.springframework.gradle.propdeps.PropDepsPlugin;

public abstract class AbstractSpringJavaPlugin implements Plugin<Project> {

	@Override
	public final void apply(Project project) {
		PluginManager pluginManager = project.getPluginManager();
		pluginManager.apply(JavaPlugin.class);
		pluginManager.apply(ManagementConfigurationPlugin.class);
		// pluginManager.apply(EclipseWtpPlugin);
		// pluginManager.apply(IdeaPlugin);
		// pluginManager.apply(PropDepsPlugin);
		// pluginManager.apply(PropDepsEclipsePlugin);
		// pluginManager.apply(PropDepsIdeaPlugin);
		// pluginManager.apply("io.spring.convention.tests-configuration");
		// pluginManager.apply("io.spring.convention.integration-test");
		// pluginManager.apply("io.spring.convention.javadoc-options");
		// pluginManager.apply("io.spring.convention.checkstyle");
		// pluginManager.apply(CopyPropertiesPlugin);


		// project.jar {
		// 	manifest.attributes["Created-By"] =
		// 			"${System.getProperty("java.version")} (${System.getProperty("java.specification.vendor")})"
		// 	manifest.attributes["Implementation-Title"] = project.name
		// 	manifest.attributes["Implementation-Version"] = project.version
		// 	manifest.attributes["Automatic-Module-Name"] = project.name.replace('-', '.')
		// }
        // project.test {
        //     useJUnitPlatform()
        // }
		additionalPlugins(project);
	}

	protected abstract void additionalPlugins(Project project);

}
