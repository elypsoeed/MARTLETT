package com.elypsoeed.martlett.core.repository;

import com.elypsoeed.martlett.core.entity.UserEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<@NonNull UserEntity, @NonNull Long> {

	boolean existsByNickname(String nickname);
}
