package com.elypsoeed.martlett.user.controller;

import com.elypsoeed.martlett.user.exception.UserProfileNotFoundException;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class UserProfileExceptionHandler {

	@ExceptionHandler(UserProfileNotFoundException.class)
	ResponseEntity<@NonNull Void> handleUserProfileNotFound() {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	}
}
