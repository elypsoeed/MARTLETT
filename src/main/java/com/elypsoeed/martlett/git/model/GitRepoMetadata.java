package com.elypsoeed.martlett.git.model;

public record GitRepoMetadata(
	Long repositoryId,
	Long ownerId,
	String ownerUsername,
	String repositoryName,
	GitRepoVisibility visibility,
	String storageRelativePath
) {
}
