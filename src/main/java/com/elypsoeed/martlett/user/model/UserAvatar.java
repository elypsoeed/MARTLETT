package com.elypsoeed.martlett.user.model;

public record UserAvatar(
	String contentType,
	byte[] data
) {
}
