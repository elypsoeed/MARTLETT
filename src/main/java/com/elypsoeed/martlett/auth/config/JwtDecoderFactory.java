package com.elypsoeed.martlett.auth.config;

import com.elypsoeed.martlett.auth.config.properties.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class JwtDecoderFactory {

	private final JwtProperties jwtProperties;

	public JwtDecoder createDefaultDecoder() {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey())
			.macAlgorithm(MacAlgorithm.HS256)
			.build();
		decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(jwtProperties.issuer()));
		return decoder;
	}

	public JwtDecoder createAccessTokenDecoder() {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey())
			.macAlgorithm(MacAlgorithm.HS256)
			.build();
		decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(
                    JwtValidators.createDefaultWithIssuer(jwtProperties.issuer()), accessTokenTypeValidator()
		        )
        );
		return decoder;
    }

    private OAuth2TokenValidator<Jwt> accessTokenTypeValidator() {
        return token -> {
            String tokenType = token.getClaimAsString("token_type");
            if ("access".equals(tokenType)) {
                return OAuth2TokenValidatorResult.success();
            }

            return OAuth2TokenValidatorResult.failure(
				new OAuth2Error("invalid_token", "Token type must be access", null)
			);
		};
	}

	private SecretKey jwtSecretKey() {
		return new SecretKeySpec(jwtProperties.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
	}
}
