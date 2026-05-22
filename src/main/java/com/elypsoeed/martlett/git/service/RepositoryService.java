package com.elypsoeed.martlett.git.service;

import com.elypsoeed.martlett.auth.entity.AuthUserEntity;
import com.elypsoeed.martlett.auth.repository.AuthUserRepository;
import com.elypsoeed.martlett.git.entity.HostedRepositoryEntity;
import com.elypsoeed.martlett.git.exception.RepositoryConflictException;
import com.elypsoeed.martlett.git.repository.HostedRepositoryRepository;
import com.elypsoeed.martlett.git.storage.RepositoryStorage;
import com.elypsoeed.martlett.generated.model.CreateRepositoryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class RepositoryService {

	private static final Pattern REPOSITORY_NAME_PATTERN = Pattern.compile("^[A-Za-z0-9._-]+$");

	private final AuthUserRepository authUserRepository;
	private final HostedRepositoryRepository hostedRepositoryRepository;
	private final RepositoryStorage repositoryStorage;

	@Transactional
	public HostedRepositoryEntity createRepository(String ownerUsername, CreateRepositoryRequest createRepositoryRequest) {
		validateRepositoryName(createRepositoryRequest.getName());

		AuthUserEntity authUser = authUserRepository.findByUsername(ownerUsername)
			.orElseThrow(() -> new UsernameNotFoundException("User not found: " + ownerUsername));

		if (hostedRepositoryRepository.existsByOwnerIdAndName(authUser.getUserId(), createRepositoryRequest.getName())) {
			throw new RepositoryConflictException("Repository already exists");
		}

		String storageRelativePath = repositoryStorage.createBareRepository(authUser.getUserId(), createRepositoryRequest.getName());

		try {
			return hostedRepositoryRepository.save(toHostedRepositoryEntity(authUser, createRepositoryRequest, storageRelativePath));
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
		CreateRepositoryRequest createRepositoryRequest,
		String storageRelativePath
	) {
		HostedRepositoryEntity hostedRepository = new HostedRepositoryEntity();
		hostedRepository.setOwner(authUser.getUser());
		hostedRepository.setName(createRepositoryRequest.getName());
		hostedRepository.setStorageRelativePath(storageRelativePath);
		hostedRepository.setCreatedTimestamp(Instant.now());
		return hostedRepository;
	}

	private void validateRepositoryName(String repositoryName) {
		if (!REPOSITORY_NAME_PATTERN.matcher(repositoryName).matches()) {
			throw new IllegalArgumentException("Invalid repository name");
		}
	}
}
