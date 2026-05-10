package com.elypsoeed.martlett.auth.repository;

import com.elypsoeed.martlett.auth.entity.AuthUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthUserRepository extends JpaRepository<AuthUserEntity, Long> {

	Optional<AuthUserEntity> findByUsername(String username);

	boolean existsByUsername(String username);
}
