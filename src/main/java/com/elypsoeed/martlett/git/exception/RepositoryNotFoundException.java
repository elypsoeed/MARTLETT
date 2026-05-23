package com.elypsoeed.martlett.git.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class RepositoryNotFoundException extends RuntimeException {

	public RepositoryNotFoundException(String message) {
		super(message);
	}
}
