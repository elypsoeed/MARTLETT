package com.elypsoeed.martlett.git.filesystem;

import com.elypsoeed.martlett.git.service.GitRepoLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GitRepoPathLocator {

	private final GitRepoLookupService gitRepoLookupService;
	private final GitRepoPathProvider gitRepoPathProvider;

	public Optional<GitRepoPathLocation> findByNameAndOwner(String repositoryName, String ownerUsername) {
		return gitRepoLookupService.findByNameAndOwner(repositoryName, ownerUsername)
			.flatMap(metadata -> gitRepoPathProvider.findPath(metadata.storageRelativePath())
				.map(path -> new GitRepoPathLocation(metadata, path)));
	}
}
