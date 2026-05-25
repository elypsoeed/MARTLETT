package com.elypsoeed.martlett.user.controller;

import com.elypsoeed.martlett.common.entity.UserEntity;
import com.elypsoeed.martlett.generated.model.UpdateUserProfileRequest;
import com.elypsoeed.martlett.generated.model.UserProfileResponse;
import com.elypsoeed.martlett.user.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneOffset;

@RestController
@RequiredArgsConstructor
public class UserProfileController {

	private final UserProfileService userProfileService;

	@GetMapping(path = "/api/me")
	public ResponseEntity<@NonNull UserProfileResponse> getCurrentUser() {
		return ResponseEntity.ok(toResponse(
			userProfileService.getByUsername(SecurityContextHolder.getContext().getAuthentication().getName())
		));
	}

	@PatchMapping(path = "/api/me")
	public ResponseEntity<@NonNull UserProfileResponse> updateCurrentUser(
		@Valid @RequestBody UpdateUserProfileRequest updateUserProfileRequest
	) {
		return ResponseEntity.ok(toResponse(userProfileService.updateByUsername(
			SecurityContextHolder.getContext().getAuthentication().getName(),
			updateUserProfileRequest
		)));
	}

	@GetMapping(path = "/api/users/{username}")
	public ResponseEntity<@NonNull UserProfileResponse> getUserByUsername(@PathVariable String username) {
		return ResponseEntity.ok(toResponse(userProfileService.getByUsername(username)));
	}

	private UserProfileResponse toResponse(UserEntity user) {
		UserProfileResponse response = new UserProfileResponse()
			.id(user.getId())
			.username(user.getUsername())
			.firstName(user.getFirstName())
			.lastName(user.getLastName())
			.age(user.getAge())
			.city(user.getCity())
			.tgContact(user.getTgContact())
			.emailContact(user.getEmailContact())
			.placeOfWork(user.getPlaceOfWork())
			.registrationTimestamp(user.getRegistrationTimestamp().atOffset(ZoneOffset.UTC));

		if (user.getSex() != null) {
			response.sex(UserProfileResponse.SexEnum.valueOf(user.getSex().name()));
		}

		return response;
	}
}
