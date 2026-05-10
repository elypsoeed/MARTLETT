package com.elypsoeed.martlett.common.repository;

import com.elypsoeed.martlett.common.entity.UserEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<@NonNull UserEntity, @NonNull Long> {

	boolean existsByNickname(String nickname);
}
