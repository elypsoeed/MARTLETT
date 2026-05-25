package com.elypsoeed.martlett.common.repository;

import com.elypsoeed.martlett.common.entity.UserEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<@NonNull UserEntity, @NonNull Long> {

	boolean existsByUsername(String username);

	Optional<UserEntity> findByUsername(String username);
}
