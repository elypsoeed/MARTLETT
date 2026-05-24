package com.elypsoeed.martlett.git.transport.jgit;

import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.http.server.GitServlet;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class JGitHttpConfiguration {

	private final JGitRepoResolver jGitRepoResolver;
	private final JGitUploadPackFactory jGitUploadPackFactory;
	private final JGitReceivePackFactory jGitReceivePackFactory;

	@Bean
	ServletRegistrationBean<@NonNull GitServlet> gitServletRegistration() {
		GitServlet gitServlet = new GitServlet();
		gitServlet.setRepositoryResolver(jGitRepoResolver);
		gitServlet.setUploadPackFactory(jGitUploadPackFactory);
		gitServlet.setReceivePackFactory(jGitReceivePackFactory);

		return new ServletRegistrationBean<>(gitServlet, "/git/*");
	}
}
