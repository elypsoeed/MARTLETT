package com.elypsoeed.martlett.git.handle;

import com.elypsoeed.martlett.auth.entity.AuthUserEntity;
import com.elypsoeed.martlett.auth.repository.AuthUserRepository;
import com.elypsoeed.martlett.git.entity.GitRepoEntity;
import com.elypsoeed.martlett.git.exception.GitRepoNotFoundException;
import com.elypsoeed.martlett.git.repository.GitRepoRepository;
import com.elypsoeed.martlett.git.storage.GitRepoStorageTransactionCoordinator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteGitRepo {

	private final AuthUserRepository authUserRepository;
	private final GitRepoRepository gitRepoRepository;
	private final GitRepoStorageTransactionCoordinator gitRepoStorageTransactionCoordinator;

	public void execute(String ownerUsername, String repositoryName) {
		AuthUserEntity authUser = authUserRepository.findByUsername(ownerUsername)
			.orElseThrow(() -> new UsernameNotFoundException("User not found: " + ownerUsername));

		GitRepoEntity gitRepo = gitRepoRepository
			.findByNameAndOwnerId(repositoryName, authUser.getUserId())
			.orElseThrow(() -> new GitRepoNotFoundException(repositoryName));

		gitRepoRepository.delete(gitRepo);
		gitRepoStorageTransactionCoordinator.deleteRepoStorageAfterCommit(gitRepo.getStorageRelativePath());
	}
}
