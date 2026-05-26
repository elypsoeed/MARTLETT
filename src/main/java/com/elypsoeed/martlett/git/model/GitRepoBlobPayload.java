package com.elypsoeed.martlett.git.model;

public record GitRepoBlobPayload(
	String name,
	String path,
	String ref,
	GitRepoCommit commit,
	byte[] content,
	Long size
) {
}
