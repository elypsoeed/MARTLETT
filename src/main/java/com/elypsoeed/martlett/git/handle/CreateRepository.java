package com.elypsoeed.martlett.git.handle;

import com.elypsoeed.martlett.auth.entity.AuthUserEntity;
import com.elypsoeed.martlett.auth.repository.AuthUserRepository;
import com.elypsoeed.martlett.git.entity.GitRepositoryEntity;
import com.elypsoeed.martlett.git.exception.RepositoryConflictException;
import com.elypsoeed.martlett.git.repository.GitRepositoryRepository;
import com.elypsoeed.martlett.git.storage.RepositoryStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class CreateRepository {

	private final AuthUserRepository authUserRepository;
	private final GitRepositoryRepository gitRepositoryRepository;
	private final RepositoryStorage repositoryStorage;

	public GitRepositoryEntity execute(String ownerUsername, String repositoryName) {
		AuthUserEntity authUser = authUserRepository.findByUsername(ownerUsername)
			.orElseThrow(() -> new UsernameNotFoundException("User not found: " + ownerUsername));

		if (gitRepositoryRepository.existsByOwnerIdAndName(authUser.getUserId(), repositoryName)) {
			throw new RepositoryConflictException("Repository already exists");
		}

		String storageRelativePath = repositoryStorage.createRepositoryStorage(authUser.getUserId(), repositoryName);

		try {
			return gitRepositoryRepository.save(toGitRepositoryEntity(authUser, repositoryName, storageRelativePath));
		} catch (DataIntegrityViolationException exception) {
			repositoryStorage.deleteRepositoryStorage(storageRelativePath);
			throw new RepositoryConflictException("Repository already exists");
		} catch (RuntimeException exception) {
			repositoryStorage.deleteRepositoryStorage(storageRelativePath);
			throw exception;
		}
	}

	private GitRepositoryEntity toGitRepositoryEntity(
		AuthUserEntity authUser,
		String repositoryName,
		String storageRelativePath
	) {
		GitRepositoryEntity gitRepository = new GitRepositoryEntity();
		gitRepository.setOwner(authUser.getUser());
		gitRepository.setName(repositoryName);
		gitRepository.setStorageRelativePath(storageRelativePath);
		gitRepository.setCreatedTimestamp(Instant.now());
		return gitRepository;
	}
}
