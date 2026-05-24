package com.elypsoeed.martlett.git.filesystem;

import com.elypsoeed.martlett.git.service.GitRepositoryLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GitRepositoryPathLocator {

	private final GitRepositoryLookupService gitRepositoryLookupService;
	private final RepositoryPathProvider repositoryPathProvider;

	public Optional<GitRepositoryPathLocation> findByNameAndOwner(String repositoryName, String ownerUsername) {
		return gitRepositoryLookupService.findByNameAndOwner(repositoryName, ownerUsername)
			.flatMap(metadata -> repositoryPathProvider.findPath(metadata.storageRelativePath())
				.map(path -> new GitRepositoryPathLocation(metadata, path)));
	}
}
