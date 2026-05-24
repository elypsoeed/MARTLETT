package com.elypsoeed.martlett.git.transport.jgit;

import com.elypsoeed.martlett.git.model.GitRepoMetadata;
import com.elypsoeed.martlett.git.service.GitRepoAccessPolicy;
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

	private final GitRepoAccessPolicy gitRepoAccessPolicy;

	@Override
	public UploadPack create(HttpServletRequest request, Repository repository) throws ServiceNotAuthorizedException {
		GitRepoMetadata metadata = (GitRepoMetadata) request.getAttribute(
			JGitRepoResolver.GIT_REPO_METADATA_ATTRIBUTE
		);
		if (metadata != null && !gitRepoAccessPolicy.resolve(metadata, request.getRemoteUser()).canRead()) {
			throw new ServiceNotAuthorizedException();
		}

		return new UploadPack(repository);
	}
}
