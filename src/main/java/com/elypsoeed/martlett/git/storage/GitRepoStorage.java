package com.elypsoeed.martlett.git.storage;

public interface GitRepoStorage {

	String createRepoStorage(long ownerId, String repositoryName);

	void deleteRepoStorage(String storageRelativePath);
}
