package com.elypsoeed.martlett.git.repository;

import com.elypsoeed.martlett.git.entity.GitRepoEntity;
import com.elypsoeed.martlett.git.model.enums.GitRepoVisibility;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GitRepoRepository extends JpaRepository<@NonNull GitRepoEntity, @NonNull Long> {

	boolean existsByNameAndOwnerId(String name, Long ownerId);

	Optional<GitRepoEntity> findByNameAndOwnerId(String repositoryName, Long ownerId);

	List<GitRepoEntity> findAllByOwnerIdOrderByCreatedTimestampDesc(Long ownerId);

	List<GitRepoEntity> findAllByOwnerIdAndVisibilityOrderByCreatedTimestampDesc(Long ownerId, GitRepoVisibility visibility);
}
