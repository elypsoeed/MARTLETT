package com.elypsoeed.martlett.git.service;

import com.elypsoeed.martlett.auth.entity.AuthUserEntity;
import com.elypsoeed.martlett.auth.repository.AuthUserRepository;
import com.elypsoeed.martlett.git.model.GitRepositoryMetadata;
import com.elypsoeed.martlett.git.repository.GitRepositoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GitRepositoryLookupService {

	private final AuthUserRepository authUserRepository;
	private final GitRepositoryRepository gitRepositoryRepository;

	public Optional<GitRepositoryMetadata> findByNameAndOwner(String repositoryName, String ownerUsername) {
		return authUserRepository.findByUsername(ownerUsername)
			.flatMap(authUser -> toMetadata(authUser, repositoryName));
	}

	private Optional<GitRepositoryMetadata> toMetadata(AuthUserEntity authUser, String repositoryName) {
		return gitRepositoryRepository.findByNameAndOwnerId(repositoryName, authUser.getUserId())
			.map(gitRepository -> new GitRepositoryMetadata(
				authUser.getUserId(),
				authUser.getUsername(),
				repositoryName,
				gitRepository.getStorageRelativePath()
			));
	}
}
