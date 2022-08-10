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

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.PluginManager;
import org.jfrog.gradle.plugin.artifactory.ArtifactoryPlugin;
import org.jfrog.gradle.plugin.artifactory.task.ArtifactoryTask;

/**
 * @author Janne Valkealahti
 */
public class ArtifactoryConventions {

	void apply(Project project) {
		PluginManager pluginManager = project.getPluginManager();
		pluginManager.apply(ArtifactoryPlugin.class);

		project.getPlugins().withType(ArtifactoryPlugin.class, artifactory -> {
			Task t = project.getTasks().findByName(ArtifactoryTask.ARTIFACTORY_PUBLISH_TASK_NAME);
			if (t != null) {
				ArtifactoryTask task = (ArtifactoryTask) t;
				task.setCiServerBuild();
				// bom is not a java project so plugin doesn't
				// add defaults for publications.
				task.publications("mavenJava");
			}
		});
	}
}
