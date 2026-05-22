package com.elypsoeed.martlett.git.storage;

import com.elypsoeed.martlett.git.config.properties.GitStorageProperties;
import com.elypsoeed.martlett.git.exception.RepositoryConflictException;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

@Component
@RequiredArgsConstructor
public class MemoryRepositoryStorage implements RepositoryStorage {

	private final GitStorageProperties gitStorageProperties;

	@Override
	public String createBareRepository(long ownerId, String repositoryName) {
		String storageRelativePath = ownerId + "/" + repositoryName + ".git";
		Path repositoryPath = repositoryPath(storageRelativePath);

		if (Files.exists(repositoryPath)) {
			throw new RepositoryConflictException("Repository already exists");
		}

		try {
			Files.createDirectories(repositoryPath.getParent());
			try (Git ignored = Git.init().setBare(true).setDirectory(repositoryPath.toFile()).call()) {
				return storageRelativePath;
			}
		} catch (IOException exception) {
			throw new IllegalStateException("Failed to prepare repository directory", exception);
		} catch (Exception exception) {
			throw new IllegalStateException("Failed to create bare repository", exception);
		}
	}

	@Override
	public void delete(String storageRelativePath) {
		Path repositoryPath = repositoryPath(storageRelativePath);
		if (!Files.exists(repositoryPath)) {
			return;
		}

		try (var paths = Files.walk(repositoryPath)) {
			paths.sorted(Comparator.reverseOrder())
				.forEach(path -> {
					try {
						Files.deleteIfExists(path);
					} catch (IOException exception) {
						throw new IllegalStateException("Failed to clean repository storage", exception);
					}
				});
		} catch (IOException exception) {
			throw new IllegalStateException("Failed to clean repository storage", exception);
		}
	}

	private Path repositoryPath(String storageRelativePath) {
		Path rootPath = Path.of(gitStorageProperties.getRootPath()).toAbsolutePath().normalize();
		Path repositoryPath = rootPath.resolve(storageRelativePath).normalize();
		if (!repositoryPath.startsWith(rootPath)) {
			throw new IllegalArgumentException("Repository path escapes storage root");
		}
		return repositoryPath;
	}
}
