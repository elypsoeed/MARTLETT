package com.elypsoeed.martlett.git.storage;

public interface RepositoryStorage {

	String createBareRepository(long ownerId, String repositoryName);

	void delete(String storageRelativePath);
}
