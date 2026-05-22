package com.elypsoeed.martlett.config;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.ApplicationContextInitializer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class TestEnvironmentInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		Path gitStorageRoot = createGitStorageRoot();
		registerCleanupHook(gitStorageRoot);
		TestPropertyValues.of("app.git.storage.root-path=" + gitStorageRoot).applyTo(applicationContext);
	}

	private Path createGitStorageRoot() {
		try {
			return Files.createTempDirectory("martlett-git-storage-").toAbsolutePath().normalize();
		}
		catch (IOException exception) {
			throw new IllegalStateException("Failed to create test git storage root", exception);
		}
	}

	private void registerCleanupHook(Path gitStorageRoot) {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> deleteRecursively(gitStorageRoot)));
	}

	private void deleteRecursively(Path rootPath) {
		if (!Files.exists(rootPath)) {
			return;
		}

		try (var paths = Files.walk(rootPath)) {
			paths.sorted(Comparator.reverseOrder())
				.forEach(path -> {
					try {
						Files.deleteIfExists(path);
					}
					catch (IOException exception) {
						throw new IllegalStateException("Failed to delete test git storage root", exception);
					}
				});
		}
		catch (IOException exception) {
			throw new IllegalStateException("Failed to delete test git storage root", exception);
		}
	}
}
