package com.elypsoeed.martlett.git.storage;

public interface RepositoryStorage {

	String createRepositoryStorage(long ownerId, String repositoryName);

	void deleteRepositoryStorage(String storageRelativePath);
}
