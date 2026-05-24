package com.elypsoeed.martlett.git.repository;

import com.elypsoeed.martlett.git.model.GitRepoPermission;
import com.elypsoeed.martlett.git.model.GitRepoPermissionSubjectType;

public interface GitRepoPermissionRepositoryCustom {

	boolean hasPermission(
		Long repositoryId,
		GitRepoPermissionSubjectType subjectType,
		Long subjectId,
		GitRepoPermission permission
	);
}
