package com.elypsoeed.martlett.auth.service;

import com.elypsoeed.martlett.auth.config.JwtDecoderFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {

	private final JwtDecoder jwtDecoder;

	public RefreshTokenService(JwtDecoderFactory jwtDecoderFactory) {
		this.jwtDecoder = jwtDecoderFactory.createDefaultDecoder();
	}

	public String extractSubject(String refreshToken) {
		Jwt jwt = jwtDecoder.decode(refreshToken);
		if (!isRefreshToken(jwt)) {
			throw new BadCredentialsException("Invalid refresh token");
		}

		return jwt.getSubject();
	}

	private boolean isRefreshToken(Jwt jwt) {
		return "refresh".equals(jwt.getClaimAsString("token_type"));
	}
}
