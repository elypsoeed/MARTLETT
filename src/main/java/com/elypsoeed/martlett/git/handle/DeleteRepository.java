package com.elypsoeed.martlett.git.handle;

import com.elypsoeed.martlett.auth.entity.AuthUserEntity;
import com.elypsoeed.martlett.auth.repository.AuthUserRepository;
import com.elypsoeed.martlett.git.entity.HostedRepositoryEntity;
import com.elypsoeed.martlett.git.exception.RepositoryNotFoundException;
import com.elypsoeed.martlett.git.repository.HostedRepositoryRepository;
import com.elypsoeed.martlett.git.storage.RepositoryStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteRepository {

	private final AuthUserRepository authUserRepository;
	private final HostedRepositoryRepository hostedRepositoryRepository;
	private final RepositoryStorage repositoryStorage;

	public void execute(String ownerUsername, String repositoryName) {
		AuthUserEntity authUser = authUserRepository.findByUsername(ownerUsername)
			.orElseThrow(() -> new UsernameNotFoundException("User not found: " + ownerUsername));

		HostedRepositoryEntity hostedRepository = hostedRepositoryRepository
			.findByNameAndOwnerId(repositoryName, authUser.getUserId())
			.orElseThrow(() -> new RepositoryNotFoundException("Repository not found"));

		hostedRepositoryRepository.delete(hostedRepository);
		repositoryStorage.delete(hostedRepository.getStorageRelativePath());
	}
}
