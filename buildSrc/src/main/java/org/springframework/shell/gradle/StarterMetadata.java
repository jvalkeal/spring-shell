package org.springframework.shell.gradle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import org.springframework.core.CollectionFactory;

public class StarterMetadata extends DefaultTask {

	private Configuration dependencies;

	private File destination;

	public StarterMetadata() {
			getInputs().property("name", (Callable<String>) () -> getProject().getName());
			getInputs().property("description", (Callable<String>) () -> getProject().getDescription());
	}

	@Classpath
	public FileCollection getDependencies() {
			return this.dependencies;
	}

	public void setDependencies(Configuration dependencies) {
			this.dependencies = dependencies;
	}

	@OutputFile
	public File getDestination() {
			return this.destination;
	}

	public void setDestination(File destination) {
			this.destination = destination;
	}

	@TaskAction
	void generateMetadata() throws IOException {
		Properties properties = CollectionFactory.createSortedProperties(true);
		properties.setProperty("name", getProject().getName());
		properties.setProperty("description", getProject().getDescription());
		properties.setProperty("dependencies", String.join(",", this.dependencies.getResolvedConfiguration()
			.getResolvedArtifacts().stream().map(ResolvedArtifact::getName).collect(Collectors.toSet())));
		this.destination.getParentFile().mkdirs();
		try (FileWriter writer = new FileWriter(this.destination)) {
			properties.store(writer, null);
		}
	}
}
