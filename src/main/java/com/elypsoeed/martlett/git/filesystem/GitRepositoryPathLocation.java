package com.elypsoeed.martlett.git.filesystem;

import com.elypsoeed.martlett.git.model.GitRepositoryMetadata;

import java.nio.file.Path;

public record GitRepositoryPathLocation(
	GitRepositoryMetadata metadata,
	Path absolutePath
) {
}
