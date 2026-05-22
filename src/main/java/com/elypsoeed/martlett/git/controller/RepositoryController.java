package com.elypsoeed.martlett.git.controller;

import com.elypsoeed.martlett.generated.api.RepositoriesApi;
import com.elypsoeed.martlett.generated.model.CreateRepositoryRequest;
import com.elypsoeed.martlett.generated.model.RepositoryResponse;
import com.elypsoeed.martlett.git.entity.HostedRepositoryEntity;
import com.elypsoeed.martlett.git.service.RepositoryService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneOffset;

@RestController
@RequiredArgsConstructor
public class RepositoryController implements RepositoriesApi {

	private final RepositoryService repositoryService;

	@Override
	public ResponseEntity<@NonNull RepositoryResponse> createRepository(CreateRepositoryRequest createRepositoryRequest) {
		HostedRepositoryEntity hostedRepository = repositoryService.createRepository(
			SecurityContextHolder.getContext().getAuthentication().getName(),
			createRepositoryRequest
		);
		return ResponseEntity.status(HttpStatus.CREATED).body(toRepositoryResponse(hostedRepository));
	}

	private RepositoryResponse toRepositoryResponse(HostedRepositoryEntity hostedRepository) {
		return new RepositoryResponse()
			.id(hostedRepository.getId())
			.name(hostedRepository.getName())
			.ownerNickname(hostedRepository.getOwner().getNickname())
			.fullName(hostedRepository.getOwner().getNickname() + "/" + hostedRepository.getName())
			.createdTimestamp(hostedRepository.getCreatedTimestamp().atOffset(ZoneOffset.UTC));
	}
}
