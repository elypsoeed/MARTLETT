package com.elypsoeed.martlett.user.service;

import com.elypsoeed.martlett.common.entity.UserEntity;
import com.elypsoeed.martlett.common.model.Sex;
import com.elypsoeed.martlett.common.repository.UserRepository;
import com.elypsoeed.martlett.generated.model.UpdateUserProfileRequest;
import com.elypsoeed.martlett.user.exception.UserProfileNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public UserEntity getByUsername(String username) {
		return userRepository.findByUsername(username)
			.orElseThrow(UserProfileNotFoundException::new);
	}

	@Transactional
	public UserEntity updateByUsername(String username, UpdateUserProfileRequest updateUserProfileRequest) {
		UserEntity user = userRepository.findByUsername(username)
			.orElseThrow(UserProfileNotFoundException::new);

		user.setFirstName(updateUserProfileRequest.getFirstName());
		user.setLastName(updateUserProfileRequest.getLastName());
		user.setSex(mapSex(updateUserProfileRequest));
		user.setAge(updateUserProfileRequest.getAge());
		user.setCity(updateUserProfileRequest.getCity());
		user.setTgContact(updateUserProfileRequest.getTgContact());
		user.setEmailContact(updateUserProfileRequest.getEmailContact());
		user.setPlaceOfWork(updateUserProfileRequest.getPlaceOfWork());

		return userRepository.save(user);
	}

	private Sex mapSex(UpdateUserProfileRequest updateUserProfileRequest) {
		if (updateUserProfileRequest.getSex() == null) {
			return null;
		}

		return Sex.valueOf(updateUserProfileRequest.getSex().getValue());
	}
}
