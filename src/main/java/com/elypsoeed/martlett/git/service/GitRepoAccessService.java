package com.elypsoeed.martlett.git.service;

import com.elypsoeed.martlett.auth.repository.AuthUserRepository;
import com.elypsoeed.martlett.git.model.GitRepoMetadata;
import com.elypsoeed.martlett.git.model.GitRepoPermission;
import com.elypsoeed.martlett.git.model.GitRepoPermissionSubjectType;
import com.elypsoeed.martlett.git.model.GitRepoVisibility;
import com.elypsoeed.martlett.git.repository.GitRepoPermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GitRepoAccessService {

	private final AuthUserRepository authUserRepository;
	private final GitRepoPermissionRepository gitRepoPermissionRepository;

	public boolean canReadRepo(GitRepoMetadata metadata, String username) {
		if (isOwner(metadata, username)) {
			return true;
		}
		if (metadata.visibility() == GitRepoVisibility.PUBLIC) {
			return true;
		}
		return hasUserPermission(metadata.repositoryId(), username,
			List.of(GitRepoPermission.REPOSITORY_READ, GitRepoPermission.REPOSITORY_WRITE, GitRepoPermission.REPOSITORY_ADMIN));
	}

//	public boolean canWriteRepo(GitRepoMetadata metadata, String username) {
//		if (isOwner(metadata, username)) {
//			return true;
//		}
//		return hasUserPermission(metadata.repositoryId(), username,
//			List.of(GitRepoPermission.REPOSITORY_WRITE, GitRepoPermission.REPOSITORY_ADMIN));
//	}

	private boolean isOwner(GitRepoMetadata metadata, String username) {
		return username != null && username.equals(metadata.ownerUsername());
	}

	private boolean hasUserPermission(Long repositoryId, String username, List<GitRepoPermission> permissions) {
		if (username == null) {
			return false;
		}

		return authUserRepository.findByUsername(username)
			.map(authUser -> gitRepoPermissionRepository.hasPermission(
				repositoryId,
				GitRepoPermissionSubjectType.USER,
				authUser.getUserId(),
				permissions
			))
			.orElse(false);
	}
}
