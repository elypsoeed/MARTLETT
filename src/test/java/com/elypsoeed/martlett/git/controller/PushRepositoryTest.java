package com.elypsoeed.martlett.git.controller;

import com.elypsoeed.martlett.IntegrationTest;
import com.elypsoeed.martlett.auth.repository.AuthUserRepository;
import com.elypsoeed.martlett.common.testdata.TestData;
import com.elypsoeed.martlett.common.testdata.TestDataProperties;
import com.elypsoeed.martlett.common.testdata.model.TestUser;
import com.elypsoeed.martlett.generated.model.CreateRepositoryRequest;
import com.elypsoeed.martlett.generated.model.RepositoryVisibility;
import com.elypsoeed.martlett.git.config.properties.GitStorageProperties;
import com.elypsoeed.martlett.git.model.enums.GitRepoPermission;
import com.elypsoeed.martlett.git.model.enums.GitRepoRole;
import com.elypsoeed.martlett.git.repository.GitRepoRepository;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@IntegrationTest
@RequiredArgsConstructor
public class PushRepositoryTest {

	private final TestData testData;
	private final TestDataProperties testDataProperties;
	private final GitStorageProperties gitStorageProperties;
	private final GitRepoRepository gitRepoRepository;
	private final AuthUserRepository authUserRepository;
	private TestInfo testInfo;

	@BeforeEach
	void setUp(TestInfo testInfo) {
		this.testInfo = testInfo;
	}

	@Test
	void ownerCanPushToPrivateRepository(@TempDir Path tempDir) throws Exception {
		TestUser owner = testData.createAuthedUser(testInfo);
		String repositoryName = "push-owner-success";
		testData.createPrivateRepository(owner, repositoryName);

		try (Git git = cloneRepository(owner, owner.username(), repositoryName, tempDir.resolve("repository"))) {
			createCommit(git, tempDir.resolve("repository"), "owner.txt", "owner");
			git.push()
				.setCredentialsProvider(new UsernamePasswordCredentialsProvider(owner.username(), owner.password()))
				.call();
		}

		assertThat(headExists(owner.username(), repositoryName)).isTrue();
		assertThat(readFileFromHead(owner.username(), repositoryName, "owner.txt")).isEqualTo("owner");
	}

	@Test
	void ownerCanPushToPublicRepository(@TempDir Path tempDir) throws Exception {
		TestUser owner = testData.createAuthedUser(testInfo);
		String repositoryName = "push-public-success";
		testData.createRepositoryFromRequest(
			owner,
			new CreateRepositoryRequest()
				.name(repositoryName)
				.visibility(RepositoryVisibility.PUBLIC)
		);

		try (Git git = cloneRepository(owner, owner.username(), repositoryName, tempDir.resolve("repository"))) {
			createCommit(git, tempDir.resolve("repository"), "public.txt", "public");
			git.push()
				.setCredentialsProvider(new UsernamePasswordCredentialsProvider(owner.username(), owner.password()))
				.call();
		}

		assertThat(headExists(owner.username(), repositoryName)).isTrue();
		assertThat(readFileFromHead(owner.username(), repositoryName, "public.txt")).isEqualTo("public");
	}

	@Test
	void writerRoleCanPushToPrivateRepository(@TempDir Path tempDir) throws Exception {
		TestUser owner = testData.createAuthedUser(testInfo);
		TestUser collaborator = testData.createAuthedUser(testInfo);
		String repositoryName = "push-writer-success";
		testData.createPrivateRepository(owner, repositoryName);
		testData.grantRole(owner.username(), repositoryName, collaborator.username(), GitRepoRole.WRITER);

		try (Git git = cloneRepository(collaborator, owner.username(), repositoryName, tempDir.resolve("repository"))) {
			createCommit(git, tempDir.resolve("repository"), "writer.txt", "writer");
			git.push()
				.setCredentialsProvider(new UsernamePasswordCredentialsProvider(
					collaborator.username(),
					collaborator.password()
				))
				.call();
		}

		assertThat(headExists(owner.username(), repositoryName)).isTrue();
		assertThat(readFileFromHead(owner.username(), repositoryName, "writer.txt")).isEqualTo("writer");
	}

	@Test
	void readPermissionDoesNotGrantPush(@TempDir Path tempDir) throws Exception {
		TestUser owner = testData.createAuthedUser(testInfo);
		TestUser collaborator = testData.createAuthedUser(testInfo);
		String repositoryName = "push-read-denied";
		testData.createPrivateRepository(owner, repositoryName);
		testData.grantPermission(owner.username(), repositoryName, collaborator.username(), GitRepoPermission.REPOSITORY_READ);

		try (Git git = cloneRepository(collaborator, owner.username(), repositoryName, tempDir.resolve("repository"))) {
			createCommit(git, tempDir.resolve("repository"), "reader.txt", "reader");
			assertThatThrownBy(() -> git.push()
				.setCredentialsProvider(new UsernamePasswordCredentialsProvider(
					collaborator.username(),
					collaborator.password()
				))
				.call())
				.isInstanceOf(TransportException.class);
		}
	}

	@Test
	void noAuth(@TempDir Path tempDir) throws Exception {
		TestUser owner = testData.createAuthedUser(testInfo);
		String repositoryName = "push-no-auth-denied";
		testData.createPrivateRepository(owner, repositoryName);

		try (Git git = initRepository(tempDir.resolve("repository"))) {
			createCommit(git, tempDir.resolve("repository"), "anon.txt", "anonymous");
			git.remoteAdd()
				.setName("origin")
				.setUri(new org.eclipse.jgit.transport.URIish(
					testDataProperties.gitRepositoryUrl(owner.username(), repositoryName)
				))
				.call();

			assertThatThrownBy(() -> git.push().setRemote("origin").call())
				.isInstanceOf(TransportException.class);
		}
	}

	@Test
	void rejectsNonFastForwardPush(@TempDir Path tempDir) throws Exception {
		TestUser owner = testData.createAuthedUser(testInfo);
		String repositoryName = "push-non-fast-forward";
		testData.createPrivateRepository(owner, repositoryName);

		Path firstCloneDir = tempDir.resolve("repository-one");
		Path secondCloneDir = tempDir.resolve("repository-two");

		try (
			Git firstClone = cloneRepository(owner, owner.username(), repositoryName, firstCloneDir);
			Git secondClone = cloneRepository(owner, owner.username(), repositoryName, secondCloneDir)
		) {
			UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(
				owner.username(),
				owner.password()
			);

			createCommit(firstClone, firstCloneDir, "first.txt", "first");
			firstClone.push()
				.setCredentialsProvider(credentialsProvider)
				.call();

			createCommit(secondClone, secondCloneDir, "second.txt", "second");
			Iterable<PushResult> pushResults = secondClone.push()
				.setCredentialsProvider(credentialsProvider)
				.call();

			assertThat(pushResults)
				.singleElement()
				.satisfies(pushResult -> assertThat(pushResult.getRemoteUpdate(Constants.R_HEADS + Constants.MASTER).getStatus())
					.isEqualTo(RemoteRefUpdate.Status.REJECTED_NONFASTFORWARD));
		}

		assertThat(readFileFromHead(owner.username(), repositoryName, "first.txt")).isEqualTo("first");
		assertThatThrownBy(() -> readFileFromHead(owner.username(), repositoryName, "second.txt"))
			.isInstanceOf(IllegalArgumentException.class);
	}

	private Git cloneRepository(TestUser testUser, String ownerUsername, String repositoryName, Path directory) throws Exception {
		return testData.cloneRepository(testUser, ownerUsername, repositoryName, directory);
	}

	private Git initRepository(Path directory) throws Exception {
		Files.createDirectories(directory);
		return Git.init().setDirectory(directory.toFile()).setInitialBranch(Constants.MASTER).call();
	}

	private void createCommit(Git git, Path repositoryDirectory, String fileName, String contents) throws Exception {
		Path filePath = repositoryDirectory.resolve(fileName);
		Files.writeString(filePath, contents);
		git.add().addFilepattern(fileName).call();
		git.commit()
			.setMessage("Add " + fileName)
			.setAuthor("Test User", "test@example.com")
			.setCommitter("Test User", "test@example.com")
			.call();
	}

	private boolean headExists(String ownerUsername, String repositoryName) throws Exception {
		try (var repository = new FileRepositoryBuilder()
			.setGitDir(repositoryPath(ownerUsername, repositoryName).toFile())
			.build()) {
			return repository.exactRef(Constants.R_HEADS + Constants.MASTER) != null;
		}
	}

	private String readFileFromHead(String ownerUsername, String repositoryName, String fileName) throws Exception {
		try (var repository = new FileRepositoryBuilder()
			.setGitDir(repositoryPath(ownerUsername, repositoryName).toFile())
			.build()) {
			var headRef = repository.exactRef(Constants.R_HEADS + Constants.MASTER);
			if (headRef == null) {
				throw new IllegalArgumentException("Repository has no master branch");
			}

			try (RevWalk revWalk = new RevWalk(repository)) {
				RevCommit headCommit = revWalk.parseCommit(headRef.getObjectId());
				try (TreeWalk treeWalk = TreeWalk.forPath(repository, fileName, headCommit.getTree())) {
					if (treeWalk == null) {
						throw new IllegalArgumentException("File not found in repository head: " + fileName);
					}
					return new String(repository.open(treeWalk.getObjectId(0)).getBytes());
				}
			}
		}
	}

	private Path repositoryPath(String ownerUsername, String repositoryName) {
		var owner = authUserRepository.findByUsername(ownerUsername).orElseThrow();
		var gitRepo = gitRepoRepository.findByNameAndOwnerId(repositoryName, owner.getUserId()).orElseThrow();
		return Path.of(gitStorageProperties.getRootPath())
			.toAbsolutePath()
			.normalize()
			.resolve(gitRepo.getStorageRelativePath());
	}
}
