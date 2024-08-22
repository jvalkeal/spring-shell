package org.springframework.shell.gradle.compresstree;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * @author Lars Grefer
 */
public class CompressTreePlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		project.getConvention().create(CompressFileOperations.class, "compressTree", CompressFileOperationsImpl.class, project);

		project.getExtensions().create(CompressFileOperations.class, "commonsCompress", CompressFileOperationsImpl.class, project);
	}
}
