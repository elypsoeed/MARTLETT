package com.elypsoeed.martlett.git.service;

import com.elypsoeed.martlett.auth.entity.AuthUserEntity;
import com.elypsoeed.martlett.auth.repository.AuthUserRepository;
import com.elypsoeed.martlett.git.entity.GitRepoEntity;
import com.elypsoeed.martlett.git.exception.GitRepoNotFoundException;
import com.elypsoeed.martlett.git.filesystem.GitRepoPathLocation;
import com.elypsoeed.martlett.git.filesystem.GitRepoPathProvider;
import com.elypsoeed.martlett.git.model.GitRepoBlob;
import com.elypsoeed.martlett.git.model.GitRepoBlobPayload;
import com.elypsoeed.martlett.git.model.GitRepoBranch;
import com.elypsoeed.martlett.git.model.GitRepoCommit;
import com.elypsoeed.martlett.git.model.GitRepoMetadata;
import com.elypsoeed.martlett.git.model.GitRepoTree;
import com.elypsoeed.martlett.git.model.enums.GitRepoVisibility;
import com.elypsoeed.martlett.git.repository.GitRepoRepository;
import com.elypsoeed.martlett.git.transport.GitRepoBrowser;
import com.elypsoeed.martlett.user.exception.UserProfileNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GitRepoReadService {

	private final AuthUserRepository authUserRepository;
	private final GitRepoRepository gitRepoRepository;
	private final GitRepoAccessPolicy gitRepoAccessPolicy;
	private final GitRepoPathProvider gitRepoPathProvider;
	private final GitRepoBrowser gitRepoBrowser;

	@Transactional(readOnly = true)
	public List<GitRepoEntity> getCurrentUserRepositories(String username) {
		return authUserRepository.findByUsername(username)
			.map(authUser -> gitRepoRepository.findAllByOwnerIdOrderByCreatedTimestampDesc(authUser.getUserId()))
			.orElseThrow(UserProfileNotFoundException::new);
	}

	@Transactional(readOnly = true)
	public List<GitRepoEntity> getPublicRepositoriesByUsername(String username) {
		return authUserRepository.findByUsername(username)
			.map(authUser -> gitRepoRepository.findAllByOwnerIdAndVisibilityOrderByCreatedTimestampDesc(
				authUser.getUserId(),
				GitRepoVisibility.PUBLIC
			))
			.orElseThrow(UserProfileNotFoundException::new);
	}

	@Transactional(readOnly = true)
	public GitRepoEntity getRepository(String ownerUsername, String repositoryName, String actorUsername) {
		var metadata = getReadableRepositoryMetadata(ownerUsername, repositoryName, actorUsername);

		return gitRepoRepository.findById(metadata.repositoryId())
			.orElseThrow(() -> new GitRepoNotFoundException(repositoryName));
	}

	@Transactional(readOnly = true)
	public List<GitRepoBranch> getRepositoryBranches(String ownerUsername, String repositoryName, String actorUsername) {
		return gitRepoBrowser.listBranches(getRepositoryLocation(
			ownerUsername,
			repositoryName,
			actorUsername
		).absolutePath());
	}

	@Transactional(readOnly = true)
	public GitRepoTree getRepositoryTree(
		String ownerUsername,
		String repositoryName,
		String actorUsername,
		String ref,
		String path
	) {
		return gitRepoBrowser.getTree(getRepositoryLocation(
			ownerUsername,
			repositoryName,
			actorUsername
		).absolutePath(), ref, path);
	}

	@Transactional(readOnly = true)
	public GitRepoBlob getRepositoryBlob(
		String ownerUsername,
		String repositoryName,
		String actorUsername,
		String ref,
		String path
	) {
		GitRepoBlobPayload blobPayload = gitRepoBrowser.getBlobPayload(getRepositoryLocation(
			ownerUsername,
			repositoryName,
			actorUsername
		).absolutePath(), ref, path);
		return toPreviewBlob(blobPayload);
	}

	@Transactional(readOnly = true)
	public GitRepoBlobPayload getRepositoryRawBlob(
		String ownerUsername,
		String repositoryName,
		String actorUsername,
		String ref,
		String path
	) {
		return gitRepoBrowser.getBlobPayload(getRepositoryLocation(
			ownerUsername,
			repositoryName,
			actorUsername
		).absolutePath(), ref, path);
	}

	@Transactional(readOnly = true)
	public GitRepoBlob getRepositoryReadme(String ownerUsername, String repositoryName, String actorUsername, String ref) {
		GitRepoBlobPayload blobPayload = gitRepoBrowser.getReadmePayload(getRepositoryLocation(
			ownerUsername,
			repositoryName,
			actorUsername
		).absolutePath(), ref);
		return toPreviewBlob(blobPayload);
	}

	@Transactional(readOnly = true)
	public List<GitRepoCommit> getRepositoryCommits(
		String ownerUsername,
		String repositoryName,
		String actorUsername,
		String ref,
		String path,
		int limit
	) {
		return gitRepoBrowser.listCommits(getRepositoryLocation(
			ownerUsername,
			repositoryName,
			actorUsername
		).absolutePath(), ref, path, limit);
	}

	public Optional<GitRepoMetadata> findMetadataByNameAndOwnerUsername(String repositoryName, String ownerUsername) {
		return authUserRepository.findByUsername(ownerUsername)
			.flatMap(authUser -> toMetadata(authUser, repositoryName));
	}

	private GitRepoMetadata getReadableRepositoryMetadata(String ownerUsername, String repositoryName, String actorUsername) {
		var metadata = findMetadataByNameAndOwnerUsername(repositoryName, ownerUsername)
			.orElseThrow(() -> new GitRepoNotFoundException(repositoryName));

		if (!gitRepoAccessPolicy.resolve(metadata, actorUsername).canRead()) {
			throw new AccessDeniedException("Repository %s is not readable".formatted(repositoryName));
		}

		return metadata;
	}

	private GitRepoPathLocation getRepositoryLocation(
		String ownerUsername,
		String repositoryName,
		String actorUsername
	) {
		GitRepoMetadata metadata = getReadableRepositoryMetadata(ownerUsername, repositoryName, actorUsername);
		return gitRepoPathProvider.findPath(metadata.storageRelativePath())
			.map(path -> new GitRepoPathLocation(metadata, path))
			.orElseThrow(() -> new GitRepoNotFoundException(repositoryName));
	}

	private Optional<GitRepoMetadata> toMetadata(AuthUserEntity authUser, String repositoryName) {
		return gitRepoRepository.findByNameAndOwnerId(repositoryName, authUser.getUserId())
			.map(gitRepo -> new GitRepoMetadata(
				gitRepo.getId(),
				authUser.getUserId(),
				authUser.getUsername(),
				repositoryName,
				gitRepo.getVisibility(),
				gitRepo.getStorageRelativePath()
			));
	}

	private GitRepoBlob toPreviewBlob(GitRepoBlobPayload blobPayload) {
		boolean binary = isBinary(blobPayload.content());
		return new GitRepoBlob(
			blobPayload.name(),
			blobPayload.path(),
			blobPayload.ref(),
			blobPayload.commit(),
			binary ? null : new String(blobPayload.content(), StandardCharsets.UTF_8),
			binary,
			blobPayload.size()
		);
	}

	private boolean isBinary(byte[] bytes) {
		for (byte value : bytes) {
			if (value == 0) {
				return true;
			}
		}

		String content = new String(bytes, StandardCharsets.UTF_8);
		return content.contains("\uFFFD");
	}
}
