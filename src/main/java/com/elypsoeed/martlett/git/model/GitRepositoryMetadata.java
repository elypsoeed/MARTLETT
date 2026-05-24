package com.elypsoeed.martlett.git.model;

public record GitRepositoryMetadata(
	Long ownerId,
	String ownerUsername,
	String repositoryName,
	String storageRelativePath
) {
}
