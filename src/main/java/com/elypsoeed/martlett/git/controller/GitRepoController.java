package com.elypsoeed.martlett.git.controller;

import com.elypsoeed.martlett.auth.context.CurrentUserProvider;
import com.elypsoeed.martlett.generated.model.CreateRepositoryRequest;
import com.elypsoeed.martlett.generated.model.RepositoryBlobResponse;
import com.elypsoeed.martlett.generated.model.RepositoryBranchResponse;
import com.elypsoeed.martlett.generated.model.RepositoryCommitResponse;
import com.elypsoeed.martlett.generated.model.RepositoryResponse;
import com.elypsoeed.martlett.generated.model.RepositoryTreeResponse;
import com.elypsoeed.martlett.git.entity.GitRepoEntity;
import com.elypsoeed.martlett.git.mapper.GitRepoResponseMapper;
import com.elypsoeed.martlett.git.model.enums.GitRepoVisibility;
import com.elypsoeed.martlett.git.service.GitRepoManagementService;
import com.elypsoeed.martlett.git.service.GitRepoReadService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GitRepoController {

	private final GitRepoManagementService gitRepoManagementService;
	private final GitRepoReadService gitRepoReadService;
	private final GitRepoResponseMapper gitRepoResponseMapper;
	private final CurrentUserProvider currentUserProvider;

	@PostMapping(path = "/api/repositories")
	public ResponseEntity<@NonNull RepositoryResponse> createRepo(@RequestBody CreateRepositoryRequest createRepositoryRequest) {
		GitRepoEntity gitRepo = gitRepoManagementService.createRepo(
			currentUserProvider.username(),
			createRepositoryRequest.getName(),
			createRepositoryRequest.getVisibility() == null
				? GitRepoVisibility.PRIVATE
				: GitRepoVisibility.valueOf(createRepositoryRequest.getVisibility().getValue())
		);
		return ResponseEntity.status(HttpStatus.CREATED).body(gitRepoResponseMapper.toRepositoryResponse(gitRepo));
	}

	@DeleteMapping(path = "/api/repositories/{repositoryName}")
	public ResponseEntity<@NonNull Void> deleteRepo(@PathVariable String repositoryName) {
		gitRepoManagementService.deleteRepo(
			currentUserProvider.username(),
			repositoryName
		);
		return ResponseEntity.noContent().build();
	}

	@GetMapping(path = "/api/repositories")
	public ResponseEntity<@NonNull List<RepositoryResponse>> getCurrentUserRepositories() {
		return ResponseEntity.ok(gitRepoReadService.getCurrentUserRepositories(
			currentUserProvider.username()
		).stream().map(gitRepoResponseMapper::toRepositoryResponse).toList());
	}

	@GetMapping(path = "/api/users/{username}/repositories")
	public ResponseEntity<@NonNull List<RepositoryResponse>> getUserPublicRepositories(@PathVariable String username) {
		return ResponseEntity.ok(gitRepoReadService.getPublicRepositoriesByUsername(username).stream()
			.map(gitRepoResponseMapper::toRepositoryResponse)
			.toList());
	}

	@GetMapping(path = "/api/repositories/{username}/{repositoryName}")
	public ResponseEntity<@NonNull RepositoryResponse> getRepository(
		@PathVariable String username,
		@PathVariable String repositoryName
	) {
		return ResponseEntity.ok(gitRepoResponseMapper.toRepositoryResponse(gitRepoReadService.getRepository(
			username,
			repositoryName,
			currentUserProvider.username()
		)));
	}

	@GetMapping(path = "/api/repositories/{username}/{repositoryName}/branches")
	public ResponseEntity<@NonNull List<RepositoryBranchResponse>> getRepositoryBranches(
		@PathVariable String username,
		@PathVariable String repositoryName
	) {
		return ResponseEntity.ok(gitRepoReadService.getRepositoryBranches(
			username,
			repositoryName,
			currentUserProvider.username()
		).stream().map(gitRepoResponseMapper::toBranchResponse).toList());
	}

	@GetMapping(path = "/api/repositories/{username}/{repositoryName}/tree")
	public ResponseEntity<@NonNull RepositoryTreeResponse> getRepositoryTree(
		@PathVariable String username,
		@PathVariable String repositoryName,
		@RequestParam String ref,
		@RequestParam(required = false) String path
	) {
		return ResponseEntity.ok(gitRepoResponseMapper.toTreeResponse(gitRepoReadService.getRepositoryTree(
			username,
			repositoryName,
			currentUserProvider.username(),
			ref,
			path
		)));
	}

	@GetMapping(path = "/api/repositories/{username}/{repositoryName}/blob")
	public ResponseEntity<@NonNull RepositoryBlobResponse> getRepositoryBlob(
		@PathVariable String username,
		@PathVariable String repositoryName,
		@RequestParam String ref,
		@RequestParam String path
	) {
		return ResponseEntity.ok(gitRepoResponseMapper.toBlobResponse(gitRepoReadService.getRepositoryBlob(
			username,
			repositoryName,
			currentUserProvider.username(),
			ref,
			path
		)));
	}

	@GetMapping(path = "/api/repositories/{username}/{repositoryName}/blob/raw", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<byte[]> getRepositoryBlobRaw(
		@PathVariable String username,
		@PathVariable String repositoryName,
		@RequestParam String ref,
		@RequestParam String path
	) {
		return ResponseEntity.ok(gitRepoReadService.getRepositoryRawBlob(
			username,
			repositoryName,
			currentUserProvider.username(),
			ref,
			path
		).content());
	}

	@GetMapping(path = "/api/repositories/{username}/{repositoryName}/readme")
	public ResponseEntity<@NonNull RepositoryBlobResponse> getRepositoryReadme(
		@PathVariable String username,
		@PathVariable String repositoryName,
		@RequestParam String ref
	) {
		return ResponseEntity.ok(gitRepoResponseMapper.toBlobResponse(gitRepoReadService.getRepositoryReadme(
			username,
			repositoryName,
			currentUserProvider.username(),
			ref
		)));
	}

	@GetMapping(path = "/api/repositories/{username}/{repositoryName}/commits")
	public ResponseEntity<@NonNull List<RepositoryCommitResponse>> getRepositoryCommits(
		@PathVariable String username,
		@PathVariable String repositoryName,
		@RequestParam String ref,
		@RequestParam(required = false) String path,
		@RequestParam(defaultValue = "20") int limit
	) {
		return ResponseEntity.ok(gitRepoReadService.getRepositoryCommits(
			username,
			repositoryName,
			currentUserProvider.username(),
			ref,
			path,
			limit
		).stream().map(gitRepoResponseMapper::toCommitResponse).toList());
	}
}
