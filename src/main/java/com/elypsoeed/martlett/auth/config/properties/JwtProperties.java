package com.elypsoeed.martlett.auth.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
	String secret,
	long accessTokenTtlSeconds,
	long refreshTokenTtlSeconds,
	String issuer
) {
	public JwtProperties {
		if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
			throw new IllegalArgumentException("app.jwt.secret must be at least 256 bits");
		}
	}
}
