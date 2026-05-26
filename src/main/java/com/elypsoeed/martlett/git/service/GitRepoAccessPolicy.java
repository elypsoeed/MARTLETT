package com.elypsoeed.martlett.git.service;

import com.elypsoeed.martlett.auth.repository.AuthUserRepository;
import com.elypsoeed.martlett.git.entity.GitRepoRoleEntity;
import com.elypsoeed.martlett.git.model.GitRepoMetadata;
import com.elypsoeed.martlett.git.model.enums.GitRepoPermissionSubjectType;
import com.elypsoeed.martlett.git.repository.GitRepoPermissionRepository;
import com.elypsoeed.martlett.git.repository.GitRepoRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GitRepoAccessPolicy {

	private final AuthUserRepository authUserRepository;
	private final GitRepoPermissionRepository gitRepoPermissionRepository;
	private final GitRepoRoleRepository gitRepoRoleRepository;

	public GitRepoAccess resolve(GitRepoMetadata metadata, String username) {
		if (username == null) {
			return new GitRepoAccess(metadata, null, List.of(), gitRepoPermissionRepository);
		}

		return authUserRepository.findByUsername(username)
			.map(authUser -> new GitRepoAccess(
				metadata,
				authUser.getUserId(),
				gitRepoRoleRepository.findAllByRepoIdAndSubjectTypeAndSubjectId(
					metadata.repositoryId(),
					GitRepoPermissionSubjectType.USER,
					authUser.getUserId()
				).stream()
					.map(GitRepoRoleEntity::getRole)
					.toList(),
				gitRepoPermissionRepository
			))
			.orElseGet(() -> new GitRepoAccess(metadata, null, List.of(), gitRepoPermissionRepository));
	}
}
