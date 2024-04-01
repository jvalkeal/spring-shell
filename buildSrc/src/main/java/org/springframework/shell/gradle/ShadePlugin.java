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

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;

import com.github.jengelman.gradle.plugins.shadow.ShadowJavaPlugin;
import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin;
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar;
import com.google.common.base.Suppliers;

public class ShadePlugin implements Plugin<Project> {

	public static final String SHADE_CONFIGURATION_NAME = "shade";

	@Override
	public void apply(Project project) {
		PluginManager pluginManager = project.getPluginManager();
		pluginManager.apply(ShadowPlugin.class);

		TaskProvider<ShadowJar> shadowJarProvider = project.getTasks().withType(ShadowJar.class)
				.named(ShadowJavaPlugin.SHADOW_JAR_TASK_NAME);

		setupShadowJarToShadeTheCorrectDependencies(project, shadowJarProvider);
		ensureShadowJarHasDefaultClassifierThatDoesNotClashWithTheRegularJarTask(project, shadowJarProvider);
		ensureShadowJarIsOnlyArtifactOnJavaConfigurations(project, shadowJarProvider);
		dependOnJarTaskInOrderToTriggerTasksAddingManifestAttributes(project, shadowJarProvider);
	}

	private void setupShadowJarToShadeTheCorrectDependencies(Project project,
			TaskProvider<ShadowJar> shadowJarProvider) {

		ConfigurationContainer configurations = project.getConfigurations();
		Configuration management = configurations.getByName(ManagementConfigurationPlugin.MANAGEMENT_CONFIGURATION_NAME);
		Configuration shade = configurations.create(SHADE_CONFIGURATION_NAME, conf -> {
			conf.setCanBeConsumed(false);
			conf.setVisible(false);
			conf.extendsFrom(management);
		});

		project.getExtensions().getByType(SourceSetContainer.class).configureEach(sourceSet -> Stream.of(
						sourceSet.getCompileClasspathConfigurationName(),
						sourceSet.getRuntimeClasspathConfigurationName())
				.map(project.getConfigurations()::getByName)
				.forEach(conf -> conf.extendsFrom(shade)));

				Supplier<ShadowingCalculation> shadowingCalculation = Suppliers.memoize(() -> {
					Set<ResolvedDependency> shadedModules = shade
							.getResolvedConfiguration()
							.getLenientConfiguration()
							.getAllModuleDependencies();

					Set<ResolvedDependency> acceptedModules = new HashSet<>(shadedModules);
					Set<ResolvedDependency> highestLevelRejectedModulesThatArentDirectlyListed = Collections.emptySet();
					return new ShadowingCalculation(acceptedModules, highestLevelRejectedModulesThatArentDirectlyListed);
				});


        // TaskProvider<ShadowJarConfigurationTask> shadowJarConfigurationTask = project.getTasks()
        //         .register("relocateShadowJar", ShadowJarConfigurationTask.class, relocateTask -> {
        //             relocateTask.getShadowJar().set(shadowJarProvider.get());

        //             relocateTask.getPrefix().set(project.provider(() -> String.join(
        //                             ".", "shadow", project.getGroup().toString(), project.getName())
        //                     .replace('-', '_')
        //                     .toLowerCase(Locale.US)));

        //             relocateTask.getAcceptedDependencies().set(project.provider(() -> shadowingCalculation
        //                     .get()
        //                     .acceptedShadedModules()));
        //         });


		shadowJarProvider.configure(shadowJar -> {
			// shadowJar.dependsOn(shadowJarConfigurationTask);
			shadowJar.setConfigurations(Collections.singletonList(shade));
		});

	}

    private static void ensureShadowJarHasDefaultClassifierThatDoesNotClashWithTheRegularJarTask(
            Project project, TaskProvider<ShadowJar> shadowJarProvider) {

        project.getTasks().withType(Jar.class).named("jar").configure(jar -> jar.getArchiveClassifier()
                .set("thin"));

        shadowJarProvider.configure(
                shadowJar -> shadowJar.getArchiveClassifier().set((String) null));
    }

    private static void ensureShadowJarIsOnlyArtifactOnJavaConfigurations(
            Project project, TaskProvider<ShadowJar> shadowJarProvider) {

        Stream.of(JavaPlugin.API_ELEMENTS_CONFIGURATION_NAME, JavaPlugin.RUNTIME_ELEMENTS_CONFIGURATION_NAME)
                .map(project.getConfigurations()::named)
                .forEach(provider -> provider.configure(conf -> {
                    conf.getOutgoing().getArtifacts().clear();
                    conf.getOutgoing().artifact(shadowJarProvider);
                }));
    }

    private static void dependOnJarTaskInOrderToTriggerTasksAddingManifestAttributes(
            Project project, TaskProvider<ShadowJar> shadowJarProvider) {
        shadowJarProvider.configure(shadowJar ->
                shadowJar.dependsOn(project.getTasks().withType(Jar.class).named("jar")));
    }


	private record ShadowingCalculation(Set<ResolvedDependency> acceptedShadedModules, Set<ResolvedDependency> rejectedShadedModules) {
	}

}
