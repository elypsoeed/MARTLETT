package com.elypsoeed.martlett.git.transport.jgit;

import jakarta.servlet.http.HttpServletRequest;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.UploadPack;
import org.eclipse.jgit.transport.resolver.UploadPackFactory;
import org.springframework.stereotype.Component;

@Component
public class JGitUploadPackFactory implements UploadPackFactory<HttpServletRequest> {

	@Override
	public UploadPack create(HttpServletRequest request, Repository repository) {
		return new UploadPack(repository);
	}
}
