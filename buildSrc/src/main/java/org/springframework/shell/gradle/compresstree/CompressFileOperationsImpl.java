package org.springframework.shell.gradle.compresstree;

import static org.gradle.api.internal.lambdas.SerializableLambdas.transformer;

import java.io.File;

import org.gradle.api.InvalidUserCodeException;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.RegularFile;
import org.gradle.api.internal.file.DefaultFileOperations;
import org.gradle.api.internal.file.FileOperations;
import org.gradle.api.internal.file.archive.DecompressionCoordinator;
import org.gradle.api.internal.file.collections.DirectoryFileTreeFactory;
import org.gradle.api.internal.file.temp.TemporaryFileProvider;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.internal.provider.ProviderInternal;
import org.gradle.api.internal.tasks.TaskDependencyFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.resources.ReadableResource;
import org.gradle.api.resources.internal.ReadableResourceInternal;
import org.gradle.api.tasks.util.PatternSet;
import org.gradle.internal.Cast;
import org.gradle.internal.Factory;
import org.gradle.internal.hash.FileHasher;
import org.gradle.internal.nativeintegration.filesystem.FileSystem;

/**
 * @author Lars Grefer
 *
 * @see FileOperations
 * @see DefaultFileOperations
 */
public class CompressFileOperationsImpl implements CompressFileOperations {

	private final FileOperations fileOperations;

	private final TemporaryFileProvider temporaryFileProvider;
	private final FileHasher fileHasher;
	private final FileSystem fileSystem;
	private final DirectoryFileTreeFactory directoryFileTreeFactory;
	private final Factory<PatternSet> patternSetFactory;
	private final TaskDependencyFactory taskDependencyFactory;
	private final ProviderFactory providers;
	private final DecompressionCoordinator decompressionCoordinator;

	public CompressFileOperationsImpl(ProjectInternal project) {
		fileOperations = project.getFileOperations();

		temporaryFileProvider = project.getServices().get(TemporaryFileProvider.class);
		fileHasher = project.getServices().get(FileHasher.class);
		fileSystem = project.getServices().get(FileSystem.class);
		directoryFileTreeFactory = project.getServices().get(DirectoryFileTreeFactory.class);
		patternSetFactory = project.getServices().getFactory(PatternSet.class);
		taskDependencyFactory = project.getServices().get(TaskDependencyFactory.class);
		providers = project.getServices().get(ProviderFactory.class);
		decompressionCoordinator = project.getServices().get(DecompressionCoordinator.class);
	}

	// @Override
	// public FileTree arTree(Object arPath) {
	//     Provider<File> file = asFileProvider(arPath);
	//     ArFileTree arFileTree = new ArFileTree(file, f -> new ArArchiveInputStream(new FileInputStream(f)), fileSystem, directoryFileTreeFactory, fileHasher, decompressionCoordinator, temporaryFileProvider);
	//     return new FileTreeAdapter(arFileTree, taskDependencyFactory, patternSetFactory);
	// }

	// @Override
	// public FileTree arjTree(Object arjFile) {
	//     return arjTree(arjFile, f -> new ArjArchiveInputStream(new FileInputStream(f)));
	// }

	// @Override
	// public FileTree arjTree(Object arjFile, String charsetName) {
	//     return arjTree(arjFile, f -> new ArjArchiveInputStream(new FileInputStream(f), charsetName));
	// }

	// private FileTree arjTree(Object arjFile, ArchiveInputStreamProvider<ArjArchiveInputStream> inputStreamProvider) {
	//     Provider<File> file = asFileProvider(arjFile);
	//     ArjFileTree arjFileTree = new ArjFileTree(file, inputStreamProvider, fileSystem, directoryFileTreeFactory, fileHasher, decompressionCoordinator, temporaryFileProvider);
	//     return new FileTreeAdapter(arjFileTree, taskDependencyFactory, patternSetFactory);
	// }

	// @Override
	// public FileTree cpioTree(Object cpioFile) {
	//     return cpioTree(cpioFile, f -> new CpioArchiveInputStream(new FileInputStream(f)));
	// }

	// @Override
	// public FileTree cpioTree(Object cpioFile, int blockSize) {
	//     return cpioTree(cpioFile, f -> new CpioArchiveInputStream(new FileInputStream(f), blockSize));
	// }

	// @Override
	// public FileTree cpioTree(Object cpioFile, String encoding) {
	//     return cpioTree(cpioFile, f -> new CpioArchiveInputStream(new FileInputStream(f), encoding));
	// }

	// @Override
	// public FileTree cpioTree(Object cpioFile, int blockSize, String encoding) {
	//     return cpioTree(cpioFile, f -> new CpioArchiveInputStream(new FileInputStream(f), blockSize, encoding));
	// }

	// private FileTree cpioTree(Object arPath, ArchiveInputStreamProvider<CpioArchiveInputStream> inputStreamProvider) {
	//     Provider<File> file = asFileProvider(arPath);
	//     ArchiveFileTree<CpioArchiveInputStream, CpioArchiveEntry> cpioFileTree = new ArchiveFileTree<>(file, inputStreamProvider, fileSystem, directoryFileTreeFactory, fileHasher, decompressionCoordinator, temporaryFileProvider);
	//     return new FileTreeAdapter(cpioFileTree, taskDependencyFactory, patternSetFactory);
	// }

	// @Override
	// public FileTree sevenZipTree(Object sevenZipFile) {
	//     return sevenZipTree(sevenZipFile, f -> new SevenZipArchiveInputStream(new SevenZFile(f)));
	// }

	// @Override
	// public FileTree sevenZipTree(Object sevenZipFile, char[] password) {
	//     return sevenZipTree(sevenZipFile, f -> new SevenZipArchiveInputStream(new SevenZFile(f, password)));
	// }

	// private FileTree sevenZipTree(Object sevenZipFile, ArchiveInputStreamProvider<SevenZipArchiveInputStream> inputStreamProvider) {
	//     Provider<File> file = asFileProvider(sevenZipFile);
	//     SevenZipFileTree sevenZipFileTree = new SevenZipFileTree(file, inputStreamProvider, fileSystem, directoryFileTreeFactory, fileHasher, decompressionCoordinator, temporaryFileProvider);
	//     return new FileTreeAdapter(sevenZipFileTree, taskDependencyFactory, patternSetFactory);
	// }

	// @Override
	// public FileTree dumpTree(Object dumpFile) {
	//     return dumpTree(dumpFile, f -> new DumpArchiveInputStream(new FileInputStream(f)));
	// }

	// @Override
	// public FileTree dumpTree(Object dumpFile, String encoding) {
	//     return dumpTree(dumpFile, f -> new DumpArchiveInputStream(new FileInputStream(f), encoding));
	// }

	// private FileTree dumpTree(Object dumpFile, ArchiveInputStreamProvider<DumpArchiveInputStream> inputStreamProvider) {
	//     Provider<File> file = asFileProvider(dumpFile);
	//     DumpFileTree dumpFileTree = new DumpFileTree(file, inputStreamProvider, fileSystem, directoryFileTreeFactory, fileHasher, decompressionCoordinator, temporaryFileProvider);
	//     return new FileTreeAdapter(dumpFileTree, taskDependencyFactory, patternSetFactory);
	// }

	public FileTree tarXzTree(Object tarXzFile) {
		File file = fileOperations.file(tarXzFile);
		return fileOperations.tarTree(new XzArchiver(file));
	}

	// public FileTree tarLzmaTree(Object tarLzmaFile) {
	//     File file = fileOperations.file(tarLzmaFile);
	//     return fileOperations.tarTree(new LzmaArchiver(file));
	// }

	/**
	 * @see DefaultFileOperations#asFileProvider(Object)
	 */
	private Provider<File> asFileProvider(Object path) {
		if (path instanceof ReadableResource) {
			boolean hasBackingFile = path instanceof ReadableResourceInternal
					&& ((ReadableResourceInternal) path).getBackingFile() != null;
			if (!hasBackingFile) {
				throw new InvalidUserCodeException("Cannot use tarTree() on a resource without a backing file.");
			}
			return providers.provider(() -> ((ReadableResourceInternal) path).getBackingFile());
		}
		if (path instanceof Provider) {
			ProviderInternal<?> provider = (ProviderInternal<?>) path;
			Class<?> type = provider.getType();
			if (type != null) {
				if (File.class.isAssignableFrom(type)) {
					return Cast.uncheckedCast(path);
				}
				if (RegularFile.class.isAssignableFrom(type)) {
					Provider<RegularFile> regularFileProvider = Cast.uncheckedCast(provider);
					return regularFileProvider.map(transformer(RegularFile::getAsFile));
				}
			}
			return provider.map(transformer(fileOperations::file));
		}
		return providers.provider(() -> fileOperations.file(path));
	}
}
