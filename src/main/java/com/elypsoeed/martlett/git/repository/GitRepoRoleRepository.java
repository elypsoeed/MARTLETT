package com.elypsoeed.martlett.git.repository;

import com.elypsoeed.martlett.git.entity.GitRepoRoleEntity;
import com.elypsoeed.martlett.git.model.GitRepoPermissionSubjectType;
import com.elypsoeed.martlett.git.model.GitRepoRole;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GitRepoRoleRepository extends JpaRepository<@NonNull GitRepoRoleEntity, @NonNull Long> {

	List<GitRepoRoleEntity> findAllByRepoIdAndSubjectTypeAndSubjectId(
		Long repositoryId,
		GitRepoPermissionSubjectType subjectType,
		Long subjectId
	);

	boolean existsByRepoIdAndSubjectTypeAndSubjectIdAndRole(
		Long repositoryId,
		GitRepoPermissionSubjectType subjectType,
		Long subjectId,
		GitRepoRole role
	);
}
