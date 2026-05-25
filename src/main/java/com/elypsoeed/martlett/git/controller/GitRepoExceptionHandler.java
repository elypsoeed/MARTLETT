package com.elypsoeed.martlett.git.controller;

import com.elypsoeed.martlett.git.exception.InvalidGitRepoNameException;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GitRepoExceptionHandler {

	@ExceptionHandler(InvalidGitRepoNameException.class)
	ResponseEntity<@NonNull Void> handleInvalidRepositoryName() {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
	}

	@ExceptionHandler(AccessDeniedException.class)
	ResponseEntity<@NonNull Void> handleAccessDenied() {
		return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
	}
}
