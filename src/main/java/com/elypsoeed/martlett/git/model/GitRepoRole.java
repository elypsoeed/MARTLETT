package com.elypsoeed.martlett.git.model;

public enum GitRepoRole {
	READER,
	WRITER;

	public boolean grants(GitRepoPermission permission) {
		return switch (this) {
			case READER -> permission == GitRepoPermission.REPOSITORY_READ;
			case WRITER -> permission == GitRepoPermission.REPOSITORY_READ
				|| permission == GitRepoPermission.REPOSITORY_WRITE;
		};
	}
}
