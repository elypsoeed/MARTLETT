package com.elypsoeed.martlett.git.transport.jgit;

import com.elypsoeed.martlett.git.model.GitRepoMetadata;
import com.elypsoeed.martlett.git.service.GitRepoAccess;
import com.elypsoeed.martlett.git.service.GitRepoAccessPolicy;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ReceivePack;
import org.eclipse.jgit.transport.resolver.ReceivePackFactory;
import org.eclipse.jgit.transport.resolver.ServiceNotAuthorizedException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JGitReceivePackFactory implements ReceivePackFactory<HttpServletRequest> {

	private final GitRepoAccessPolicy gitRepoAccessPolicy;

	@Override
	public ReceivePack create(HttpServletRequest request, Repository repository) throws ServiceNotAuthorizedException {
		GitRepoMetadata metadata = (GitRepoMetadata) request.getAttribute(
			JGitRepoResolver.GIT_REPO_METADATA_ATTRIBUTE
		);
		GitRepoAccess access = gitRepoAccessPolicy.resolve(metadata, request.getRemoteUser());
		if (!access.canWrite()) {
			throw new ServiceNotAuthorizedException();
		}

		return createReceivePack(repository, access);
	}

	private ReceivePack createReceivePack(Repository repository, GitRepoAccess access) {
        return new ReceivePack(repository);
	}
}
