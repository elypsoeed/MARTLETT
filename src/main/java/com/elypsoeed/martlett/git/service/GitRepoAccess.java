package com.elypsoeed.martlett.git.service;

import com.elypsoeed.martlett.git.model.GitRepoMetadata;
import com.elypsoeed.martlett.git.model.GitRepoPermission;
import com.elypsoeed.martlett.git.model.GitRepoPermissionSubjectType;
import com.elypsoeed.martlett.git.model.GitRepoRole;
import com.elypsoeed.martlett.git.model.GitRepoVisibility;
import com.elypsoeed.martlett.git.repository.GitRepoPermissionRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class GitRepoAccess {

	private final GitRepoMetadata metadata;
	private final Long userId;
	private final List<GitRepoRole> roles;
	private final GitRepoPermissionRepository gitRepoPermissionRepository;

	public boolean canRead() {
		if (isOwner()) {
			return true;
		}
		if (metadata.visibility() == GitRepoVisibility.PUBLIC) {
			return true;
		}
		return hasPermission(GitRepoPermission.REPOSITORY_READ);
	}

	public boolean canWrite() {
		if (isOwner()) {
			return true;
		}
		return hasPermission(GitRepoPermission.REPOSITORY_WRITE);
	}

	private boolean isOwner() {
		return userId != null && userId.equals(metadata.ownerId());
	}

	private boolean hasPermission(GitRepoPermission permission) {
		if (userId == null) {
			return false;
		}
		if (roles.stream().anyMatch(role -> role.grants(permission))) {
			return true;
		}

		return gitRepoPermissionRepository.hasPermission(
			metadata.repositoryId(),
			GitRepoPermissionSubjectType.USER,
			userId,
			permission
		);
	}
}
