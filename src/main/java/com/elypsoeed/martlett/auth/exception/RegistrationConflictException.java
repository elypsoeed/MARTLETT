package com.elypsoeed.martlett.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class RegistrationConflictException extends RuntimeException {

	public RegistrationConflictException(String message) {
		super(message);
	}
}
