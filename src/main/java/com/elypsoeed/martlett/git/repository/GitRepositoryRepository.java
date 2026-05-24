package com.elypsoeed.martlett.git.repository;

import com.elypsoeed.martlett.git.entity.GitRepositoryEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GitRepositoryRepository extends JpaRepository<@NonNull GitRepositoryEntity, @NonNull Long> {

	boolean existsByOwnerIdAndName(Long ownerId, String name);

	Optional<GitRepositoryEntity> findByNameAndOwnerId(String repositoryName, Long ownerId);
}
