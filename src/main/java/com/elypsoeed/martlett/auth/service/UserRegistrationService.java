package com.elypsoeed.martlett.auth.service;

import com.elypsoeed.martlett.auth.entity.AuthUserEntity;
import com.elypsoeed.martlett.auth.exception.RegistrationConflictException;
import com.elypsoeed.martlett.auth.model.Role;
import com.elypsoeed.martlett.auth.repository.AuthUserRepository;
import com.elypsoeed.martlett.core.entity.UserEntity;
import com.elypsoeed.martlett.core.model.Sex;
import com.elypsoeed.martlett.core.repository.UserRepository;
import com.elypsoeed.martlett.generated.model.RegisterUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserRegistrationService {

	private final UserRepository userRepository;
	private final AuthUserRepository authUserRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public void registerUser(RegisterUserRequest registerUserRequest) {
		if (authUserRepository.existsByUsername(registerUserRequest.getUsername())) {
			throw new RegistrationConflictException("Username already exists");
		}

		if (userRepository.existsByNickname(registerUserRequest.getNickname())) {
			throw new RegistrationConflictException("Nickname already exists");
		}

		try {
			UserEntity user = userRepository.save(toUserEntity(registerUserRequest));
			authUserRepository.save(toAuthUserEntity(user, registerUserRequest));
		} catch (DataIntegrityViolationException exception) {
			throw new RegistrationConflictException("Username or nickname already exists");
		}
	}

	private UserEntity toUserEntity(RegisterUserRequest registerUserRequest) {
		UserEntity user = new UserEntity();
		user.setName(registerUserRequest.getName());
		user.setSurname(registerUserRequest.getSurname());
		user.setNickname(registerUserRequest.getNickname());
		user.setRegistrationTimestamp(Instant.now());
		user.setSex(mapSex(registerUserRequest));
		user.setAge(registerUserRequest.getAge());
		user.setCity(registerUserRequest.getCity());
		user.setTgContact(registerUserRequest.getTgContact());
		user.setEmailContact(registerUserRequest.getEmailContact());
		user.setPlaceOfWork(registerUserRequest.getPlaceOfWork());
		return user;
	}

	private AuthUserEntity toAuthUserEntity(UserEntity user, RegisterUserRequest registerUserRequest) {
		AuthUserEntity authUser = new AuthUserEntity();
		authUser.setUser(user);
		authUser.setUsername(registerUserRequest.getUsername());
		authUser.setPasswordHash(passwordEncoder.encode(registerUserRequest.getPassword()));
		authUser.setRoles(Set.of(Role.USER));
		return authUser;
	}

	private Sex mapSex(RegisterUserRequest registerUserRequest) {
		if (registerUserRequest.getSex() == null) {
			return null;
		}

		return Sex.valueOf(registerUserRequest.getSex().getValue());
	}
}
