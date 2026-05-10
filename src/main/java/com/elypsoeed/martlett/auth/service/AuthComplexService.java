package com.elypsoeed.martlett.auth.service;

import com.elypsoeed.martlett.auth.config.properties.JwtProperties;
import com.elypsoeed.martlett.generated.model.RegisterUserRequest;
import com.elypsoeed.martlett.generated.model.RefreshTokenRequest;
import com.elypsoeed.martlett.generated.model.TokenRequest;
import com.elypsoeed.martlett.generated.model.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class AuthComplexService {

	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final JwtProperties jwtProperties;
	private final RefreshTokenService refreshTokenService;
	private final DatabaseUserDetailsService userDetailsService;
	private final UserRegistrationService userRegistrationService;

	public void registerUser(RegisterUserRequest registerUserRequest) {
		userRegistrationService.registerUser(registerUserRequest);
	}

	public TokenResponse issueToken(TokenRequest tokenRequest) {
		Authentication authentication = authenticationManager.authenticate(
			UsernamePasswordAuthenticationToken.unauthenticated(
				tokenRequest.getUsername(),
				tokenRequest.getPassword()
			)
		);

		return issueTokenPair(authentication.getName(), authentication.getAuthorities());
	}

	public TokenResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
		UserDetails userDetails = userDetailsService
			.loadUserByUsername(refreshTokenService.extractSubject(refreshTokenRequest.getRefreshToken()));
		return issueTokenPair(userDetails.getUsername(), userDetails.getAuthorities());
	}

	private TokenResponse issueTokenPair(String username, Collection<? extends GrantedAuthority> authorities) {
		return new TokenResponse()
			.accessToken(jwtService.issueAccessToken(username, authorities))
			.accessExpiresIn(jwtProperties.accessTokenTtlSeconds())
			.refreshToken(jwtService.issueRefreshToken(username, authorities))
			.refreshExpiresIn(jwtProperties.refreshTokenTtlSeconds())
			.tokenType("Bearer");
	}
}
