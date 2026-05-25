package com.elypsoeed.martlett.git.model;

import com.elypsoeed.martlett.git.model.enums.GitRepoTreeEntryType;

public record GitRepoTreeEntry(
	String name,
	String path,
	GitRepoTreeEntryType type,
	Long size
) {
}
