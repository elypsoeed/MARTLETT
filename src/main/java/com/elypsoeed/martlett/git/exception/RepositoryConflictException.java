package com.elypsoeed.martlett.git.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class RepositoryConflictException extends RuntimeException {

	public RepositoryConflictException(String message) {
		super(message);
	}
}
