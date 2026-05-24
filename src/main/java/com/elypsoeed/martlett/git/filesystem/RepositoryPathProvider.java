package com.elypsoeed.martlett.git.filesystem;

import java.nio.file.Path;
import java.util.Optional;

public interface RepositoryPathProvider {

	Optional<Path> findPath(String storageRelativePath);
}
