package com.elypsoeed.martlett.git.handle;

import com.elypsoeed.martlett.auth.entity.AuthUserEntity;
import com.elypsoeed.martlett.auth.repository.AuthUserRepository;
import com.elypsoeed.martlett.git.entity.HostedRepositoryEntity;
import com.elypsoeed.martlett.git.exception.RepositoryConflictException;
import com.elypsoeed.martlett.git.repository.HostedRepositoryRepository;
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
	private final HostedRepositoryRepository hostedRepositoryRepository;
	private final RepositoryStorage repositoryStorage;

	public HostedRepositoryEntity execute(String ownerUsername, String repositoryName) {
		AuthUserEntity authUser = authUserRepository.findByUsername(ownerUsername)
			.orElseThrow(() -> new UsernameNotFoundException("User not found: " + ownerUsername));

		if (hostedRepositoryRepository.existsByOwnerIdAndName(authUser.getUserId(), repositoryName)) {
			throw new RepositoryConflictException("Repository already exists");
		}

		String storageRelativePath = repositoryStorage.createBareRepository(authUser.getUserId(), repositoryName);

		try {
			return hostedRepositoryRepository.save(toHostedRepositoryEntity(authUser, repositoryName, storageRelativePath));
		} catch (DataIntegrityViolationException exception) {
			repositoryStorage.delete(storageRelativePath);
			throw new RepositoryConflictException("Repository already exists");
		} catch (RuntimeException exception) {
			repositoryStorage.delete(storageRelativePath);
			throw exception;
		}
	}

	private HostedRepositoryEntity toHostedRepositoryEntity(
		AuthUserEntity authUser,
		String repositoryName,
		String storageRelativePath
	) {
		HostedRepositoryEntity hostedRepository = new HostedRepositoryEntity();
		hostedRepository.setOwner(authUser.getUser());
		hostedRepository.setName(repositoryName);
		hostedRepository.setStorageRelativePath(storageRelativePath);
		hostedRepository.setCreatedTimestamp(Instant.now());
		return hostedRepository;
	}
}
