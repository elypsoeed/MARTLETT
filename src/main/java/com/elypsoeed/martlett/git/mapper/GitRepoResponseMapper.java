package com.elypsoeed.martlett.git.mapper;

import com.elypsoeed.martlett.generated.model.RepositoryBlobResponse;
import com.elypsoeed.martlett.generated.model.RepositoryBranchResponse;
import com.elypsoeed.martlett.generated.model.RepositoryCommitResponse;
import com.elypsoeed.martlett.generated.model.RepositoryResponse;
import com.elypsoeed.martlett.generated.model.RepositoryTreeResponse;
import com.elypsoeed.martlett.generated.model.RepositoryTreeEntryResponse;
import com.elypsoeed.martlett.generated.model.RepositoryVisibility;
import com.elypsoeed.martlett.git.entity.GitRepoEntity;
import com.elypsoeed.martlett.git.model.GitRepoBlob;
import com.elypsoeed.martlett.git.model.GitRepoBranch;
import com.elypsoeed.martlett.git.model.GitRepoCommit;
import com.elypsoeed.martlett.git.model.GitRepoTree;
import com.elypsoeed.martlett.git.model.GitRepoTreeEntry;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;

@Component
public class GitRepoResponseMapper {

	public RepositoryResponse toRepositoryResponse(GitRepoEntity gitRepo) {
		return new RepositoryResponse()
			.id(gitRepo.getId())
			.name(gitRepo.getName())
			.ownerNickname(gitRepo.getOwner().getUsername())
			.fullName(gitRepo.getOwner().getUsername() + "/" + gitRepo.getName())
			.visibility(RepositoryVisibility.valueOf(gitRepo.getVisibility().name()))
			.createdTimestamp(gitRepo.getCreatedTimestamp().atOffset(ZoneOffset.UTC));
	}

	public RepositoryBranchResponse toBranchResponse(GitRepoBranch branch) {
		return new RepositoryBranchResponse()
			.name(branch.name())
			.defaultBranch(branch.defaultBranch());
	}

	public RepositoryTreeEntryResponse toTreeEntryResponse(GitRepoTreeEntry entry) {
		return new RepositoryTreeEntryResponse()
			.name(entry.name())
			.path(entry.path())
			.type(RepositoryTreeEntryResponse.TypeEnum.valueOf(entry.type().name()))
			.size(entry.size());
	}

	public RepositoryBlobResponse toBlobResponse(GitRepoBlob blob) {
		return new RepositoryBlobResponse()
			.name(blob.name())
			.path(blob.path())
			.ref(blob.ref())
			.commit(toCommitResponse(blob.commit()))
			.binary(blob.binary())
			.content(blob.content())
			.size(blob.size());
	}

	public RepositoryTreeResponse toTreeResponse(GitRepoTree tree) {
		return new RepositoryTreeResponse()
			.ref(tree.ref())
			.path(tree.path())
			.commit(toCommitResponse(tree.commit()))
			.entries(tree.entries().stream().map(this::toTreeEntryResponse).toList());
	}

	public RepositoryCommitResponse toCommitResponse(GitRepoCommit commit) {
		return new RepositoryCommitResponse()
			.sha(commit.sha())
			.message(commit.message())
			.authorName(commit.authorName())
			.authorEmail(commit.authorEmail())
			.committedTimestamp(commit.committedTimestamp().atOffset(ZoneOffset.UTC));
	}
}
