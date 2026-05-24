package com.elypsoeed.martlett.git.controller;

import com.elypsoeed.martlett.generated.model.CreateRepositoryRequest;
import com.elypsoeed.martlett.generated.model.RepositoryResponse;
import com.elypsoeed.martlett.git.entity.GitRepositoryEntity;
import com.elypsoeed.martlett.git.service.GitRepositoryManagementService;
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
public class RepositoryController {

	private final GitRepositoryManagementService gitRepositoryManagementService;

	@PostMapping(path = "/api/repositories")
	public ResponseEntity<@NonNull RepositoryResponse> createRepository(@RequestBody CreateRepositoryRequest createRepositoryRequest) {
		GitRepositoryEntity gitRepository = gitRepositoryManagementService.createRepository(
			SecurityContextHolder.getContext().getAuthentication().getName(),
			createRepositoryRequest.getName()
		);
		return ResponseEntity.status(HttpStatus.CREATED).body(toRepositoryResponse(gitRepository));
	}

	@DeleteMapping(path = "/api/repositories/{repositoryName}")
	public ResponseEntity<@NonNull Void> deleteRepository(@PathVariable String repositoryName) {
		gitRepositoryManagementService.deleteRepository(
			SecurityContextHolder.getContext().getAuthentication().getName(),
			repositoryName
		);
		return ResponseEntity.noContent().build();
	}

	private RepositoryResponse toRepositoryResponse(GitRepositoryEntity gitRepository) {
		return new RepositoryResponse()
			.id(gitRepository.getId())
			.name(gitRepository.getName())
			.ownerNickname(gitRepository.getOwner().getUsername())
			.fullName(gitRepository.getOwner().getUsername() + "/" + gitRepository.getName())
			.createdTimestamp(gitRepository.getCreatedTimestamp().atOffset(ZoneOffset.UTC));
	}
}
