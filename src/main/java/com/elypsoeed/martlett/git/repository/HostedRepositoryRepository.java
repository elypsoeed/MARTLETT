package com.elypsoeed.martlett.git.repository;

import com.elypsoeed.martlett.git.entity.HostedRepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HostedRepositoryRepository extends JpaRepository<HostedRepositoryEntity, Long> {

	boolean existsByOwnerIdAndName(Long ownerId, String name);

	Optional<HostedRepositoryEntity> findByOwnerNicknameAndName(String ownerNickname, String name);
}
