package com.elypsoeed.martlett.git.filesystem;

import com.elypsoeed.martlett.git.model.GitRepoMetadata;

import java.nio.file.Path;

public record GitRepoPathLocation(
	GitRepoMetadata metadata,
	Path absolutePath
) {
}
