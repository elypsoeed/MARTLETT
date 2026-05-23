package com.elypsoeed.martlett.git.repository;

import com.elypsoeed.martlett.git.entity.HostedRepositoryEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HostedRepositoryRepository extends JpaRepository<@NonNull HostedRepositoryEntity, @NonNull Long> {

	boolean existsByOwnerIdAndName(Long ownerId, String name);

	Optional<HostedRepositoryEntity> findByNameAndOwnerId(String repositoryName, Long ownerId);
}
