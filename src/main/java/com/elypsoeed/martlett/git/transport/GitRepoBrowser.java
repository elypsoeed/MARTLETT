package com.elypsoeed.martlett.git.transport;

import com.elypsoeed.martlett.git.model.GitRepoBlobPayload;
import com.elypsoeed.martlett.git.model.GitRepoBranch;
import com.elypsoeed.martlett.git.model.GitRepoCommit;
import com.elypsoeed.martlett.git.model.GitRepoTree;

import java.nio.file.Path;
import java.util.List;

public interface GitRepoBrowser {

	List<GitRepoBranch> listBranches(Path gitDirectory);

	GitRepoTree getTree(Path gitDirectory, String ref, String path);

	GitRepoBlobPayload getBlobPayload(Path gitDirectory, String ref, String path);

	GitRepoBlobPayload getReadmePayload(Path gitDirectory, String ref);

	List<GitRepoCommit> listCommits(Path gitDirectory, String ref, String path, int limit);
}
