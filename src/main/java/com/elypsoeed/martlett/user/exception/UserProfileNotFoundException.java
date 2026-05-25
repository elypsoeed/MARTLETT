package com.elypsoeed.martlett.user.exception;

public class UserProfileNotFoundException extends RuntimeException {

	public UserProfileNotFoundException(String message) {
		super(message);
	}

    public UserProfileNotFoundException() {
        super("User not found");
    }
}
