package com.elypsoeed.martlett.git.repository;

import com.elypsoeed.martlett.git.model.GitRepoPermission;
import com.elypsoeed.martlett.git.model.GitRepoPermissionSubjectType;

import java.util.Collection;

public interface GitRepoPermissionRepositoryCustom {

	boolean hasPermission(
		Long repositoryId,
		GitRepoPermissionSubjectType subjectType,
		Long subjectId,
		Collection<GitRepoPermission> permissions
	);
}
