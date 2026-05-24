package com.elypsoeed.martlett.git.controller;

import com.elypsoeed.martlett.IntegrationTest;
import com.elypsoeed.martlett.common.testdata.TestData;
import com.elypsoeed.martlett.common.testdata.TestDataProperties;
import com.elypsoeed.martlett.common.testdata.model.TestUser;
import com.elypsoeed.martlett.generated.model.CreateRepositoryRequest;
import com.elypsoeed.martlett.generated.model.RepositoryVisibility;
import com.elypsoeed.martlett.git.model.GitRepoPermission;
import com.elypsoeed.martlett.git.model.GitRepoRole;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.Git;
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
public class CloneRepositoryTest {

	private final TestData testData;
	private final TestDataProperties testDataProperties;
	private TestInfo testInfo;

	@BeforeEach
	void setUp(TestInfo testInfo) {
		this.testInfo = testInfo;
	}

	@Test
	void noAuth(@TempDir Path tempDir) {
		TestUser testUser = testData.createAuthedUser(testInfo);
        String REPOSITORY_NAME = "clone-no-auth";

		testData.createPrivateRepository(testUser, REPOSITORY_NAME);

		assertThatThrownBy(() -> cloneRepository(
			testUser.username(),
			REPOSITORY_NAME,
			tempDir.resolve("repository")
		))
			.isInstanceOf(TransportException.class);
	}

	@Test
	void publicRepositoryCanBeClonedWithoutAuth(@TempDir Path tempDir) throws Exception {
		TestUser testUser = testData.createAuthedUser(testInfo);
        String REPOSITORY_NAME = "clone-public";

		testData.createRepositoryFromRequest(
			testUser,
			new CreateRepositoryRequest()
				.name(REPOSITORY_NAME)
				.visibility(RepositoryVisibility.PUBLIC)
		);

		Path cloneDirectory = tempDir.resolve("repository");
		try (var git = cloneRepository(testUser.username(), REPOSITORY_NAME, cloneDirectory)) {
			assertThat(Files.exists(cloneDirectory.resolve(".git"))).isTrue();
			assertThat(git.getRepository().getRemoteNames()).containsExactly("origin");
		}
	}

	@Test
	void success(@TempDir Path tempDir) throws Exception {
		TestUser testUser = testData.createAuthedUser(testInfo);
        String REPOSITORY_NAME = "clone-success";
		testData.createPrivateRepository(testUser, REPOSITORY_NAME);

		Path cloneDirectory = tempDir.resolve("repository");
		try (var git = testData.cloneRepository(testUser, testUser.username(), REPOSITORY_NAME, cloneDirectory)) {
			assertThat(Files.exists(cloneDirectory.resolve(".git"))).isTrue();
			assertThat(git.getRepository().getRemoteNames()).containsExactly("origin");
		}
	}

	@Test
	void readCollaboratorCanClonePrivateRepository(@TempDir Path tempDir) throws Exception {
		TestUser owner = testData.createAuthedUser(testInfo);
		TestUser collaborator = testData.createAuthedUser(testInfo);
		String REPOSITORY_NAME = "clone-private-collaborator";
		testData.createPrivateRepository(owner, REPOSITORY_NAME);
		testData.grantPermission(owner.username(), REPOSITORY_NAME, collaborator.username(), GitRepoPermission.REPOSITORY_READ);

		Path cloneDirectory = tempDir.resolve("repository");
		try (var git = testData.cloneRepository(
			collaborator,
			owner.username(),
			REPOSITORY_NAME,
			cloneDirectory
		)) {
			assertThat(Files.exists(cloneDirectory.resolve(".git"))).isTrue();
			assertThat(git.getRepository().getRemoteNames()).containsExactly("origin");
		}
	}

	@Test
	void writerRoleCanClonePrivateRepository(@TempDir Path tempDir) throws Exception {
		TestUser owner = testData.createAuthedUser(testInfo);
		TestUser collaborator = testData.createAuthedUser(testInfo);
		String repositoryName = "clone-private-writer";
		testData.createPrivateRepository(owner, repositoryName);
		testData.grantRole(owner.username(), repositoryName, collaborator.username(), GitRepoRole.WRITER);

		try (var git = testData.cloneRepository(
			collaborator,
			owner.username(),
			repositoryName,
			tempDir.resolve("repository")
		)) {
			assertThat(Files.exists(tempDir.resolve("repository").resolve(".git"))).isTrue();
			assertThat(git.getRepository().getRemoteNames()).containsExactly("origin");
		}
	}

	@Test
	void writePermissionDoesNotGrantRead(@TempDir Path tempDir) {
		TestUser owner = testData.createAuthedUser(testInfo);
		TestUser collaborator = testData.createAuthedUser(testInfo);
		String repositoryName = "clone-private-write-only";
		testData.createPrivateRepository(owner, repositoryName);
		testData.grantPermission(owner.username(), repositoryName, collaborator.username(), GitRepoPermission.REPOSITORY_WRITE);

		assertThatThrownBy(() -> testData.cloneRepository(
			collaborator,
			owner.username(),
			repositoryName,
			tempDir.resolve("repository")
		)).isInstanceOf(TransportException.class);
	}

	@Test
	void missingRepository(@TempDir Path tempDir) {
		TestUser testUser = testData.createAuthedUser(testInfo);

		assertThatThrownBy(() -> testData.cloneRepository(
			testUser,
			testUser.username(),
			"missing-repository",
			tempDir.resolve("repository")
		)).isInstanceOf(InvalidRemoteException.class);
	}

	private Git cloneRepository(String ownerUsername, String repositoryName, Path directory) throws Exception {
		return Git.cloneRepository()
			.setURI(testDataProperties.gitRepositoryUrl(ownerUsername, repositoryName))
			.setDirectory(directory.toFile())
			.call();
	}
}
