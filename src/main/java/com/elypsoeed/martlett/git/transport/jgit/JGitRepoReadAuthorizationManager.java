package com.elypsoeed.martlett.git.transport.jgit;

import com.elypsoeed.martlett.git.service.GitRepoAccessPolicy;
import com.elypsoeed.martlett.git.service.GitRepoLookupService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class JGitRepoReadAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

	private static final Pattern REQUEST_PATH_PATTERN = Pattern.compile("^/git/([^/]+)/([^/]+)\\.git(?:/.*)?$");

	private final GitRepoLookupService gitRepoLookupService;
	private final GitRepoAccessPolicy gitRepoAccessPolicy;

	@Override
	public AuthorizationResult authorize(
		Supplier<? extends Authentication> authentication,
		RequestAuthorizationContext requestAuthorizationContext
	) {
		HttpServletRequest request = requestAuthorizationContext.getRequest();
		Matcher matcher = REQUEST_PATH_PATTERN.matcher(request.getRequestURI());
		if (!matcher.matches()) {
			return new AuthorizationDecision(false);
		}

		String ownerUsername = matcher.group(1);
		String repositoryName = matcher.group(2);

		return new AuthorizationDecision(gitRepoLookupService.findByNameAndOwner(repositoryName, ownerUsername)
			.map(metadata -> gitRepoAccessPolicy.resolve(metadata, authenticatedUsername(authentication.get())).canRead())
			.orElse(true));
	}

	private String authenticatedUsername(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
			return null;
		}
		return authentication.getName();
	}
}
