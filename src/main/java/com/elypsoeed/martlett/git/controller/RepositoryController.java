package com.elypsoeed.martlett.git.controller;

import com.elypsoeed.martlett.generated.model.CreateRepositoryRequest;
import com.elypsoeed.martlett.generated.model.RepositoryResponse;
import com.elypsoeed.martlett.git.entity.HostedRepositoryEntity;
import com.elypsoeed.martlett.git.service.RepositoryService;
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

	private final RepositoryService repositoryService;

	@PostMapping(path = "/api/repositories")
	public ResponseEntity<@NonNull RepositoryResponse> createRepository(@RequestBody CreateRepositoryRequest createRepositoryRequest) {
		HostedRepositoryEntity hostedRepository = repositoryService.createRepository(
			SecurityContextHolder.getContext().getAuthentication().getName(),
			createRepositoryRequest.getName()
		);
		return ResponseEntity.status(HttpStatus.CREATED).body(toRepositoryResponse(hostedRepository));
	}

	@DeleteMapping(path = "/api/repositories/{repositoryName}")
	public ResponseEntity<@NonNull Void> deleteRepository(@PathVariable String repositoryName) {
		repositoryService.deleteRepository(
			SecurityContextHolder.getContext().getAuthentication().getName(),
			repositoryName
		);
		return ResponseEntity.noContent().build();
	}

	private RepositoryResponse toRepositoryResponse(HostedRepositoryEntity hostedRepository) {
		return new RepositoryResponse()
			.id(hostedRepository.getId())
			.name(hostedRepository.getName())
			.ownerNickname(hostedRepository.getOwner().getUsername())
			.fullName(hostedRepository.getOwner().getUsername() + "/" + hostedRepository.getName())
			.createdTimestamp(hostedRepository.getCreatedTimestamp().atOffset(ZoneOffset.UTC));
	}
}
