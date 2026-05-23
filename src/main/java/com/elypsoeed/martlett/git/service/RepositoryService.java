package com.elypsoeed.martlett.git.service;

import com.elypsoeed.martlett.git.entity.HostedRepositoryEntity;
import com.elypsoeed.martlett.git.handle.CreateRepository;
import com.elypsoeed.martlett.git.handle.DeleteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class RepositoryService {

	private static final Pattern REPOSITORY_NAME_PATTERN = Pattern.compile("^[A-Za-z0-9._-]+$");

	private final CreateRepository createRepository;
	private final DeleteRepository deleteRepository;

	@Transactional
	public HostedRepositoryEntity createRepository(String ownerUsername, String repositoryName) {
		validateRepositoryName(repositoryName);
		return createRepository.execute(ownerUsername, repositoryName);
	}

	@Transactional
	public void deleteRepository(String ownerUsername, String repositoryName) {
		validateRepositoryName(repositoryName);
		deleteRepository.execute(ownerUsername, repositoryName);
	}

	private void validateRepositoryName(String repositoryName) {
		if (!REPOSITORY_NAME_PATTERN.matcher(repositoryName).matches()) {
			throw new IllegalArgumentException("Invalid repository name");
		}
	}
}
