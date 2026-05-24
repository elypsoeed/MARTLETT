package com.elypsoeed.martlett.git.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class GitRepoNotFoundException extends RuntimeException {

	public GitRepoNotFoundException(String message) {
		super(message);
	}
}
