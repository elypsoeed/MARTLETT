package com.elypsoeed.martlett.git.handle;

import com.elypsoeed.martlett.auth.entity.AuthUserEntity;
import com.elypsoeed.martlett.auth.repository.AuthUserRepository;
import com.elypsoeed.martlett.git.entity.GitRepoEntity;
import com.elypsoeed.martlett.git.exception.GitRepoConflictException;
import com.elypsoeed.martlett.git.model.GitRepoVisibility;
import com.elypsoeed.martlett.git.repository.GitRepoRepository;
import com.elypsoeed.martlett.git.storage.GitRepoStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class CreateGitRepo {

	private final AuthUserRepository authUserRepository;
	private final GitRepoRepository gitRepoRepository;
	private final GitRepoStorage gitRepoStorage;

	public GitRepoEntity execute(String ownerUsername, String repositoryName, GitRepoVisibility visibility) {
		AuthUserEntity authUser = authUserRepository.findByUsername(ownerUsername)
			.orElseThrow(() -> new UsernameNotFoundException("User not found: " + ownerUsername));

		if (gitRepoRepository.existsByNameAndOwnerId(repositoryName, authUser.getUserId())) {
			throw new GitRepoConflictException("Repository already exists");
		}

		String storageRelativePath = gitRepoStorage.createRepoStorage(authUser.getUserId(), repositoryName);

		try {
			return gitRepoRepository.save(toGitRepoEntity(authUser, repositoryName, visibility, storageRelativePath));
		} catch (DataIntegrityViolationException exception) {
			gitRepoStorage.deleteRepoStorage(storageRelativePath);
			throw new GitRepoConflictException("Repository already exists");
		} catch (RuntimeException exception) {
			gitRepoStorage.deleteRepoStorage(storageRelativePath);
			throw exception;
		}
	}

	private GitRepoEntity toGitRepoEntity(
		AuthUserEntity authUser,
		String repositoryName,
		GitRepoVisibility visibility,
		String storageRelativePath
	) {
		GitRepoEntity gitRepo = new GitRepoEntity();
		gitRepo.setOwner(authUser.getUser());
		gitRepo.setName(repositoryName);
		gitRepo.setVisibility(visibility);
		gitRepo.setStorageRelativePath(storageRelativePath);
		gitRepo.setCreatedTimestamp(Instant.now());
		return gitRepo;
	}
}
