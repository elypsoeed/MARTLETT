package com.elypsoeed.martlett.git.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class GitRepoConflictException extends RuntimeException {

	public GitRepoConflictException(String message) {
		super(message);
	}
}
