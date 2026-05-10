package com.elypsoeed.martlett.auth.service;

import com.elypsoeed.martlett.auth.config.properties.JwtProperties;
import com.elypsoeed.martlett.auth.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

	private final JwtEncoder jwtEncoder;
	private final JwtProperties jwtProperties;

	public String issueAccessToken(String username, Collection<? extends GrantedAuthority> authorities) {
		return issueToken(username, "access", jwtProperties.accessTokenTtlSeconds(), roles(authorities));
	}

	public String issueRefreshToken(String username, Collection<? extends GrantedAuthority> authorities) {
		return issueToken(username, "refresh", jwtProperties.refreshTokenTtlSeconds(), roles(authorities));
	}

	public boolean isRefreshToken(Jwt jwt) {
		return "refresh".equals(jwt.getClaimAsString("token_type"));
	}

	private String issueToken(String username, String tokenType, long ttlSeconds, List<String> roles) {
		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plusSeconds(ttlSeconds);

		JwtClaimsSet claims = JwtClaimsSet.builder()
			.issuer(jwtProperties.issuer())
			.subject(username)
			.id(UUID.randomUUID().toString())
			.issuedAt(issuedAt)
			.expiresAt(expiresAt)
			.claim("token_type", tokenType)
			.claim("roles", roles)
			.build();

		JwsHeader jwsHeader = JwsHeader.with(() -> "HS256").build();
		return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
	}

	private List<String> roles(Collection<? extends GrantedAuthority> authorities) {
		return authorities.stream()
			.map(GrantedAuthority::getAuthority)
			.filter(authority -> authority.startsWith("ROLE_"))
			.map(authority -> authority.substring("ROLE_".length()))
			.filter(role -> role.equals(Role.USER.name()) || role.equals(Role.ADMIN.name()))
			.distinct()
			.toList();
	}
}
