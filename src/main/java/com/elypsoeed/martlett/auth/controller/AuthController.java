package com.elypsoeed.martlett.auth.controller;

import com.elypsoeed.martlett.auth.service.AuthComplexService;
import com.elypsoeed.martlett.generated.api.AuthApi;
import com.elypsoeed.martlett.generated.model.RegisterUserRequest;
import com.elypsoeed.martlett.generated.model.RefreshTokenRequest;
import com.elypsoeed.martlett.generated.model.TokenRequest;
import com.elypsoeed.martlett.generated.model.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

	private final AuthComplexService authComplexService;

	@Override
	public ResponseEntity<@NonNull Void> registerUser(RegisterUserRequest registerUserRequest) {
		authComplexService.registerUser(registerUserRequest);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@Override
	public ResponseEntity<@NonNull TokenResponse> issueToken(TokenRequest tokenRequest) {
		return ResponseEntity.ok(authComplexService.issueToken(tokenRequest));
	}

	@Override
	public ResponseEntity<@NonNull TokenResponse> refreshToken(RefreshTokenRequest refreshTokenRequest) {
		return ResponseEntity.ok(authComplexService.refreshToken(refreshTokenRequest));
	}
}
