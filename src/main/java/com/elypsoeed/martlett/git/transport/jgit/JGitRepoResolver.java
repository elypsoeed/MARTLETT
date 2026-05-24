package com.elypsoeed.martlett.git.transport.jgit;

import com.elypsoeed.martlett.git.filesystem.GitRepoPathLocation;
import com.elypsoeed.martlett.git.filesystem.GitRepoPathLocator;
import com.elypsoeed.martlett.git.model.GitRepoMetadata;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.resolver.RepositoryResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class JGitRepoResolver implements RepositoryResolver<HttpServletRequest> {

	private static final Pattern REPOSITORY_PATH_PATTERN = Pattern.compile("^([^/]+)/([^/]+)\\.git$");
	static final String GIT_REPO_METADATA_ATTRIBUTE = GitRepoMetadata.class.getName();

	private final GitRepoPathLocator gitRepoPathLocator;

	@Override
	public Repository open(HttpServletRequest request, String name) throws RepositoryNotFoundException {
		Matcher matcher = REPOSITORY_PATH_PATTERN.matcher(name);
		if (!matcher.matches()) {
			throw new RepositoryNotFoundException(name);
		}

		String ownerUsername = matcher.group(1);
		String repositoryName = matcher.group(2);

		GitRepoPathLocation gitRepoPathLocation = gitRepoPathLocator.findByNameAndOwner(repositoryName, ownerUsername)
			.orElseThrow(() -> new RepositoryNotFoundException(name));
		request.setAttribute(GIT_REPO_METADATA_ATTRIBUTE, gitRepoPathLocation.metadata());

		try {
			return new FileRepositoryBuilder()
				.setGitDir(gitRepoPathLocation.absolutePath().toFile())
				.build();
		} catch (IOException exception) {
			throw new RepositoryNotFoundException(name);
		}
	}
}
