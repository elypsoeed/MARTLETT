package com.elypsoeed.martlett.git.model;

import java.util.List;

public record GitRepoTree(
	String ref,
	String path,
	GitRepoCommit commit,
	List<GitRepoTreeEntry> entries
) {
}
