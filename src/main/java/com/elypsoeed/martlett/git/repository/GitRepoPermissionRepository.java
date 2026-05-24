package com.elypsoeed.martlett.git.repository;

import com.elypsoeed.martlett.git.entity.GitRepoPermissionEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GitRepoPermissionRepository
	extends JpaRepository<@NonNull GitRepoPermissionEntity, @NonNull Long>, GitRepoPermissionRepositoryCustom {
}
