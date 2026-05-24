package com.elypsoeed.martlett.git.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@RequiredArgsConstructor
public class GitRepoStorageTransactionCoordinator {

	private final GitRepoStorage gitRepoStorage;

	public String createRepoStorage(long ownerId, String repositoryName) {
		String storageRelativePath = gitRepoStorage.createRepoStorage(ownerId, repositoryName);
		registerRollbackCleanup(storageRelativePath);
		return storageRelativePath;
	}

	public void deleteRepoStorageAfterCommit(String storageRelativePath) {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			gitRepoStorage.deleteRepoStorage(storageRelativePath);
			return;
		}

		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				gitRepoStorage.deleteRepoStorage(storageRelativePath);
			}
		});
	}

	private void registerRollbackCleanup(String storageRelativePath) {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			return;
		}

		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCompletion(int status) {
				if (status == STATUS_ROLLED_BACK) {
					gitRepoStorage.deleteRepoStorage(storageRelativePath);
				}
			}
		});
	}
}
