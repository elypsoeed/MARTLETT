package com.elypsoeed.martlett.git.model;

import com.elypsoeed.martlett.git.model.enums.GitRepoVisibility;

public record GitRepoMetadata(
	Long repositoryId,
	Long ownerId,
	String ownerUsername,
	String repositoryName,
	GitRepoVisibility visibility,
	String storageRelativePath
) {
}
