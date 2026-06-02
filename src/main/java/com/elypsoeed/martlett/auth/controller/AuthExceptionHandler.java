package com.elypsoeed.martlett.auth.controller;

import com.elypsoeed.martlett.auth.exception.RegistrationConflictException;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler {

	@ExceptionHandler(RegistrationConflictException.class)
	ResponseEntity<@NonNull Void> handleRegistrationConflict() {
		return ResponseEntity.status(HttpStatus.CONFLICT).build();
	}

	@ExceptionHandler({AuthenticationException.class, JwtException.class})
	ResponseEntity<@NonNull Void> handleUnauthorized() {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	}
}
