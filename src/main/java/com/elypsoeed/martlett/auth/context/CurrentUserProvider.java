package com.elypsoeed.martlett.auth.context;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {

	public String username() {
		return SecurityContextHolder.getContext().getAuthentication().getName();
	}
}
