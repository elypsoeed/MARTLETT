package com.elypsoeed.martlett.git.controller;

import com.elypsoeed.martlett.generated.model.CreateRepositoryRequest;
import com.elypsoeed.martlett.generated.model.RepositoryVisibility;
import com.elypsoeed.martlett.generated.model.RepositoryResponse;
import com.elypsoeed.martlett.git.entity.GitRepoEntity;
import com.elypsoeed.martlett.git.model.GitRepoVisibility;
import com.elypsoeed.martlett.git.service.GitRepoManagementService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneOffset;

@RestController
@RequiredArgsConstructor
public class GitRepoController {

	private final GitRepoManagementService gitRepoManagementService;

	@PostMapping(path = "/api/repositories")
	public ResponseEntity<@NonNull RepositoryResponse> createRepo(@RequestBody CreateRepositoryRequest createRepositoryRequest) {
		GitRepoEntity gitRepo = gitRepoManagementService.createRepo(
			SecurityContextHolder.getContext().getAuthentication().getName(),
			createRepositoryRequest.getName(),
			createRepositoryRequest.getVisibility() == null
				? GitRepoVisibility.PRIVATE
				: GitRepoVisibility.valueOf(createRepositoryRequest.getVisibility().getValue())
		);
		return ResponseEntity.status(HttpStatus.CREATED).body(toRepositoryResponse(gitRepo));
	}

	@DeleteMapping(path = "/api/repositories/{repositoryName}")
	public ResponseEntity<@NonNull Void> deleteRepo(@PathVariable String repositoryName) {
		gitRepoManagementService.deleteRepo(
			SecurityContextHolder.getContext().getAuthentication().getName(),
			repositoryName
		);
		return ResponseEntity.noContent().build();
	}

	private RepositoryResponse toRepositoryResponse(GitRepoEntity gitRepo) {
		return new RepositoryResponse()
			.id(gitRepo.getId())
			.name(gitRepo.getName())
			.ownerNickname(gitRepo.getOwner().getUsername())
			.fullName(gitRepo.getOwner().getUsername() + "/" + gitRepo.getName())
			.visibility(RepositoryVisibility.valueOf(gitRepo.getVisibility().name()))
			.createdTimestamp(gitRepo.getCreatedTimestamp().atOffset(ZoneOffset.UTC));
	}
}
