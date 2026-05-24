package com.elypsoeed.martlett.git.transport.jgit;

import com.elypsoeed.martlett.git.model.GitRepoMetadata;
import com.elypsoeed.martlett.git.service.GitRepoAccessService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.UploadPack;
import org.eclipse.jgit.transport.resolver.ServiceNotAuthorizedException;
import org.eclipse.jgit.transport.resolver.UploadPackFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JGitUploadPackFactory implements UploadPackFactory<HttpServletRequest> {

	private final GitRepoAccessService gitRepoAccessService;

	@Override
	public UploadPack create(HttpServletRequest request, Repository repository) throws ServiceNotAuthorizedException {
		GitRepoMetadata metadata = (GitRepoMetadata) request.getAttribute(
			JGitRepoResolver.GIT_REPO_METADATA_ATTRIBUTE
		);
		if (metadata != null && !gitRepoAccessService.canReadRepo(metadata, request.getRemoteUser())) {
			throw new ServiceNotAuthorizedException();
		}

		return new UploadPack(repository);
	}
}
