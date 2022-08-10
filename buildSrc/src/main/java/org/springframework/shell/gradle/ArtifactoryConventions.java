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

import java.util.HashMap;
import java.util.Map;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.PluginManager;
import org.jfrog.build.extractor.clientConfiguration.ArtifactSpec;
import org.jfrog.build.extractor.clientConfiguration.ArtifactSpecs;
import org.jfrog.gradle.plugin.artifactory.ArtifactoryPlugin;
import org.jfrog.gradle.plugin.artifactory.task.ArtifactoryTask;

/**
 * @author Janne Valkealahti
 */
public class ArtifactoryConventions {

	// configure(rootProject) {
	// 	pluginManager.withPlugin('com.jfrog.artifactory') {
	// 		artifactory {
	// 			publish {
	// 				defaults {
	// 					properties {
	// 						archives '*:*:*:*@zip', 'zip.deployed': false, 'zip.name': 'spring-shell', 'zip.displayname': 'Spring Shell'
	// 						archives '*:*:*:docs@zip', 'zip.type': 'docs'
	// 						archives '*:*:*:dist@zip', 'zip.type': 'dist'
	// 					}
	// 				}
	// 			}
	// 		}
	// 	}
	// }

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
				task.publishConfigs("archives");

				t.doFirst(tt -> {
					ArtifactoryTask ttt = (ArtifactoryTask) tt;
					ArtifactSpecs artifactSpecs = ttt.getArtifactSpecs();
					Map<String, String> propsMap = new HashMap<>();
					propsMap.put("zip.deployed", "false");
					propsMap.put("zip.name", "spring-shell");
					propsMap.put("zip.displayname", "Spring Shell");
					propsMap.put("zip.type", "dist");
					ArtifactSpec spec = ArtifactSpec.builder()
						.artifactNotation("*:*:*:*@zip")
						.configuration("archives")
						.properties(propsMap)
						.build();
					artifactSpecs.add(spec);
				});

			}
		});
	}
}
