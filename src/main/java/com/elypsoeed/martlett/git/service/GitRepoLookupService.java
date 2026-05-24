package com.elypsoeed.martlett.git.service;

import com.elypsoeed.martlett.auth.entity.AuthUserEntity;
import com.elypsoeed.martlett.auth.repository.AuthUserRepository;
import com.elypsoeed.martlett.git.model.GitRepoMetadata;
import com.elypsoeed.martlett.git.repository.GitRepoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GitRepoLookupService {

	private final AuthUserRepository authUserRepository;
	private final GitRepoRepository gitRepoRepository;

	public Optional<GitRepoMetadata> findByNameAndOwner(String repositoryName, String ownerUsername) {
		return authUserRepository.findByUsername(ownerUsername)
			.flatMap(authUser -> toMetadata(authUser, repositoryName));
	}

	private Optional<GitRepoMetadata> toMetadata(AuthUserEntity authUser, String repositoryName) {
		return gitRepoRepository.findByNameAndOwnerId(repositoryName, authUser.getUserId())
			.map(gitRepo -> new GitRepoMetadata(
				gitRepo.getId(),
				authUser.getUserId(),
				authUser.getUsername(),
				repositoryName,
				gitRepo.getVisibility(),
				gitRepo.getStorageRelativePath()
			));
	}
}
