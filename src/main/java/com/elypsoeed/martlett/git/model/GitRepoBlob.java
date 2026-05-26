package com.elypsoeed.martlett.git.model;

public record GitRepoBlob(
	String name,
	String path,
	String ref,
	GitRepoCommit commit,
	String content,
	boolean binary,
	Long size
) {
}
