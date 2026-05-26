package com.elypsoeed.martlett.user.mapper;

import com.elypsoeed.martlett.common.entity.UserEntity;
import com.elypsoeed.martlett.generated.model.UserProfileResponse;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;

@Component
public class UserProfileResponseMapper {

	public UserProfileResponse toResponse(UserEntity user) {
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
