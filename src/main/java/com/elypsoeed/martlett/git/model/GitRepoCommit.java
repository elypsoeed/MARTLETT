package com.elypsoeed.martlett.git.model;

import java.time.Instant;

public record GitRepoCommit(
	String sha,
	String message,
	String authorName,
	String authorEmail,
	Instant committedTimestamp
) {
}
