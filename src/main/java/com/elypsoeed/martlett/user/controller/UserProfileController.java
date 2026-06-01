package com.elypsoeed.martlett.user.controller;

import com.elypsoeed.martlett.auth.context.CurrentUserProvider;
import com.elypsoeed.martlett.generated.model.UpdateUserProfileRequest;
import com.elypsoeed.martlett.generated.model.UserProfileResponse;
import com.elypsoeed.martlett.user.mapper.UserProfileResponseMapper;
import com.elypsoeed.martlett.user.model.UserAvatar;
import com.elypsoeed.martlett.user.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class UserProfileController {

	private final UserProfileService userProfileService;
	private final UserProfileResponseMapper userProfileResponseMapper;
	private final CurrentUserProvider currentUserProvider;

	@GetMapping(path = "/api/me")
	public ResponseEntity<@NonNull UserProfileResponse> getCurrentUser() {
		return ResponseEntity.ok(userProfileResponseMapper.toResponse(
			userProfileService.getByUsername(currentUserProvider.username())
		));
	}

	@PatchMapping(path = "/api/me")
	public ResponseEntity<@NonNull UserProfileResponse> updateCurrentUser(
		@Valid @RequestBody UpdateUserProfileRequest updateUserProfileRequest
	) {
		return ResponseEntity.ok(userProfileResponseMapper.toResponse(userProfileService.updateByUsername(
			currentUserProvider.username(),
			updateUserProfileRequest
		)));
	}

	@PutMapping(path = "/api/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<@NonNull UserProfileResponse> updateCurrentUserAvatar(@RequestPart("file") MultipartFile file) {
		return ResponseEntity.ok(userProfileResponseMapper.toResponse(userProfileService.updateAvatar(
			currentUserProvider.username(),
			file
		)));
	}

	@GetMapping(path = "/api/users/{username}")
	public ResponseEntity<@NonNull UserProfileResponse> getUserByUsername(@PathVariable String username) {
		return ResponseEntity.ok(userProfileResponseMapper.toResponse(userProfileService.getByUsername(username)));
	}

	@GetMapping(path = "/api/users/{username}/avatar")
	public ResponseEntity<byte[]> getUserAvatar(@PathVariable String username) {
		UserAvatar avatar = userProfileService.getAvatar(username);
		return ResponseEntity.ok()
			.contentType(MediaType.parseMediaType(avatar.contentType()))
			.body(avatar.data());
	}
}
