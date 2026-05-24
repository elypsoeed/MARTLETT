package com.elypsoeed.martlett.git.repository;

import com.elypsoeed.martlett.git.entity.GitRepoEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GitRepoRepository extends JpaRepository<@NonNull GitRepoEntity, @NonNull Long> {

	boolean existsByNameAndOwnerId(String name, Long ownerId);

	Optional<GitRepoEntity> findByNameAndOwnerId(String repositoryName, Long ownerId);
}
