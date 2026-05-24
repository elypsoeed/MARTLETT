package com.elypsoeed.martlett.git.transport.jgit;

import jakarta.servlet.http.HttpServletRequest;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ReceivePack;
import org.eclipse.jgit.transport.resolver.ReceivePackFactory;
import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException;
import org.springframework.stereotype.Component;

@Component
public class JGitReceivePackFactory implements ReceivePackFactory<HttpServletRequest> {

	@Override
	public ReceivePack create(HttpServletRequest request, Repository repository) throws ServiceNotEnabledException {
		throw new ServiceNotEnabledException();
	}
}
