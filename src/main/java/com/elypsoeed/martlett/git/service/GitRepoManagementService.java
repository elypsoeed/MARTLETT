package com.elypsoeed.martlett.git.service;

import com.elypsoeed.martlett.git.entity.GitRepoEntity;
import com.elypsoeed.martlett.git.handle.CreateGitRepo;
import com.elypsoeed.martlett.git.handle.DeleteGitRepo;
import com.elypsoeed.martlett.git.model.GitRepoVisibility;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class GitRepoManagementService {

	private static final Pattern REPOSITORY_NAME_PATTERN = Pattern.compile("^[A-Za-z0-9._-]+$");

	private final CreateGitRepo createGitRepo;
	private final DeleteGitRepo deleteGitRepo;

	@Transactional
	public GitRepoEntity createRepo(
		String ownerUsername,
		String repositoryName,
		GitRepoVisibility visibility
	) {
		validateRepositoryName(repositoryName);
		return createGitRepo.execute(ownerUsername, repositoryName, visibility);
	}

	@Transactional
	public void deleteRepo(String ownerUsername, String repositoryName) {
		validateRepositoryName(repositoryName);
		deleteGitRepo.execute(ownerUsername, repositoryName);
	}

	private void validateRepositoryName(String repositoryName) {
		if (!REPOSITORY_NAME_PATTERN.matcher(repositoryName).matches()) {
			throw new IllegalArgumentException("Invalid repository name");
		}
	}
}
