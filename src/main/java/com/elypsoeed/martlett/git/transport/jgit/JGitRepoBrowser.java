package com.elypsoeed.martlett.git.transport.jgit;

import com.elypsoeed.martlett.git.exception.GitRepoContentNotFoundException;
import com.elypsoeed.martlett.git.model.GitRepoBlobPayload;
import com.elypsoeed.martlett.git.model.GitRepoBranch;
import com.elypsoeed.martlett.git.model.GitRepoCommit;
import com.elypsoeed.martlett.git.model.GitRepoTree;
import com.elypsoeed.martlett.git.model.GitRepoTreeEntry;
import com.elypsoeed.martlett.git.model.enums.GitRepoTreeEntryType;
import com.elypsoeed.martlett.git.transport.GitRepoBrowser;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Component
public class JGitRepoBrowser implements GitRepoBrowser {

	private static final Set<String> README_FILE_NAMES = Set.of(
		"readme",
		"readme.md",
		"readme.txt",
		"readme.adoc",
		"readme.rst"
	);

	@Override
	public List<GitRepoBranch> listBranches(Path gitDirectory) {
		try (Repository repository = openRepository(gitDirectory)) {
			String defaultBranchName = getDefaultBranchName(repository);

			return repository.getRefDatabase().getRefsByPrefix(Constants.R_HEADS).stream()
				.map(ref -> {
					String branchName = Repository.shortenRefName(ref.getName());
					return new GitRepoBranch(branchName, branchName.equals(defaultBranchName));
				})
				.sorted(Comparator.comparing(GitRepoBranch::defaultBranch).reversed()
					.thenComparing(GitRepoBranch::name))
				.toList();
		} catch (IOException exception) {
			throw new GitRepoContentNotFoundException();
		}
	}

	@Override
	public GitRepoTree getTree(Path gitDirectory, String ref, String path) {
		try (
			Repository repository = openRepository(gitDirectory);
			RevWalk revWalk = new RevWalk(repository)
		) {
			String normalizedPath = normalizePath(path);
			RevCommit commit = resolveCommit(repository, revWalk, ref);
			RevTree tree = commit.getTree();
			if (!normalizedPath.isBlank()) {
				tree = resolveSubtree(repository, revWalk, tree, normalizedPath);
			}

			List<GitRepoTreeEntry> entries = new ArrayList<>();
			try (TreeWalk treeWalk = new TreeWalk(repository)) {
				treeWalk.addTree(tree);
				treeWalk.setRecursive(false);

				while (treeWalk.next()) {
					boolean directory = treeWalk.isSubtree();
					String name = treeWalk.getNameString();
					String entryPath = normalizedPath.isBlank() ? name : normalizedPath + "/" + name;

					entries.add(new GitRepoTreeEntry(
						name,
						entryPath,
						directory ? GitRepoTreeEntryType.DIRECTORY : GitRepoTreeEntryType.FILE,
						directory ? null : (long) repository.open(treeWalk.getObjectId(0)).getSize()
					));
				}
			}

			entries.sort(Comparator.comparing((GitRepoTreeEntry entry) -> entry.type() == GitRepoTreeEntryType.FILE)
				.thenComparing(GitRepoTreeEntry::name));
			return new GitRepoTree(
				ref,
				normalizedPath.isBlank() ? null : normalizedPath,
				toGitRepoCommit(commit),
				entries
			);
		} catch (IOException exception) {
			throw new GitRepoContentNotFoundException();
		}
	}

	@Override
	public GitRepoBlobPayload getBlobPayload(Path gitDirectory, String ref, String path) {
		try (
			Repository repository = openRepository(gitDirectory);
			RevWalk revWalk = new RevWalk(repository)
		) {
			String normalizedPath = normalizeRequiredPath(path);
			RevCommit commit = resolveCommit(repository, revWalk, ref);
			RevTree tree = commit.getTree();

			try (TreeWalk treeWalk = TreeWalk.forPath(repository, normalizedPath, tree)) {
				if (treeWalk == null || treeWalk.isSubtree()) {
					throw new GitRepoContentNotFoundException();
				}

				byte[] bytes = readBlobBytes(repository, treeWalk);
				return new GitRepoBlobPayload(
					fileName(normalizedPath),
					normalizedPath,
					ref,
					toGitRepoCommit(commit),
					bytes,
					(long) bytes.length
				);
			}
		} catch (IOException exception) {
			throw new GitRepoContentNotFoundException();
		}
	}

	@Override
	public GitRepoBlobPayload getReadmePayload(Path gitDirectory, String ref) {
		try (
			Repository repository = openRepository(gitDirectory);
			RevWalk revWalk = new RevWalk(repository)
		) {
			RevCommit commit = resolveCommit(repository, revWalk, ref);
			RevTree tree = commit.getTree();

			try (TreeWalk treeWalk = new TreeWalk(repository)) {
				treeWalk.addTree(tree);
				treeWalk.setRecursive(false);

				while (treeWalk.next()) {
					if (treeWalk.isSubtree()) {
						continue;
					}

					String name = treeWalk.getNameString();
					if (!README_FILE_NAMES.contains(name.toLowerCase())) {
						continue;
					}

					byte[] bytes = readBlobBytes(repository, treeWalk);
					return new GitRepoBlobPayload(
						name,
						name,
						ref,
						toGitRepoCommit(commit),
						bytes,
						(long) bytes.length
					);
				}
			}

			throw new GitRepoContentNotFoundException();
		} catch (IOException exception) {
			throw new GitRepoContentNotFoundException();
		}
	}

	@Override
	public List<GitRepoCommit> listCommits(Path gitDirectory, String ref, String path, int limit) {
		try (
			Repository repository = openRepository(gitDirectory);
			RevWalk revWalk = new RevWalk(repository);
			Git git = Git.wrap(repository)
		) {
			RevCommit commit = resolveCommit(repository, revWalk, ref);
			var logCommand = git.log()
				.add(commit.getId())
				.setMaxCount(limit);

			String normalizedPath = normalizePath(path);
			if (!normalizedPath.isBlank()) {
				logCommand.addPath(normalizedPath);
			}

			List<GitRepoCommit> commits = new ArrayList<>();
			for (RevCommit currentCommit : logCommand.call()) {
				commits.add(toGitRepoCommit(currentCommit));
			}
			return commits;
		} catch (IOException | GitAPIException exception) {
			throw new GitRepoContentNotFoundException();
		}
	}

	private Repository openRepository(Path gitDirectory) throws IOException {
		return new FileRepositoryBuilder()
			.setGitDir(gitDirectory.toFile())
			.build();
	}

	private String getDefaultBranchName(Repository repository) throws IOException {
		Ref head = repository.exactRef(Constants.HEAD);
		if (head == null || !head.isSymbolic()) {
			return null;
		}
		return Repository.shortenRefName(head.getTarget().getName());
	}

	private RevCommit resolveCommit(Repository repository, RevWalk revWalk, String ref) throws IOException {
		ObjectId objectId = repository.resolve(ref);
		if (objectId == null) {
			throw new GitRepoContentNotFoundException();
		}

		RevObject object = revWalk.parseAny(objectId);
		if (object instanceof RevCommit commit) {
			return commit;
		}
		throw new GitRepoContentNotFoundException();
	}

	private RevTree resolveSubtree(Repository repository, RevWalk revWalk, RevTree tree, String path) throws IOException {
		try (TreeWalk treeWalk = TreeWalk.forPath(repository, path, tree)) {
			if (treeWalk == null || !treeWalk.isSubtree()) {
				throw new GitRepoContentNotFoundException();
			}
			return revWalk.parseTree(treeWalk.getObjectId(0));
		}
	}

	private String normalizeRequiredPath(String path) {
		String normalizedPath = normalizePath(path);
		if (normalizedPath.isBlank()) {
			throw new GitRepoContentNotFoundException();
		}
		return normalizedPath;
	}

	private String normalizePath(String path) {
		if (path == null) {
			return "";
		}

		String normalizedPath = path.trim();
		while (normalizedPath.startsWith("/")) {
			normalizedPath = normalizedPath.substring(1);
		}
		while (normalizedPath.endsWith("/")) {
			normalizedPath = normalizedPath.substring(0, normalizedPath.length() - 1);
		}
		return normalizedPath;
	}

	private String fileName(String path) {
		int slashIndex = path.lastIndexOf('/');
		return slashIndex == -1 ? path : path.substring(slashIndex + 1);
	}

	private byte[] readBlobBytes(Repository repository, TreeWalk treeWalk) throws IOException {
		return repository.open(treeWalk.getObjectId(0)).getBytes();
	}

	private GitRepoCommit toGitRepoCommit(RevCommit commit) {
		return new GitRepoCommit(
			commit.getId().name(),
			commit.getShortMessage(),
			commit.getAuthorIdent().getName(),
			commit.getAuthorIdent().getEmailAddress(),
			commit.getAuthorIdent().getWhenAsInstant()
		);
	}

}
