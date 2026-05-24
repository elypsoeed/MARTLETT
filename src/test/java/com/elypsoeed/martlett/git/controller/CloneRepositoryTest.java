package com.elypsoeed.martlett.git.controller;

import com.elypsoeed.martlett.IntegrationTest;
import com.elypsoeed.martlett.common.testdata.TestData;
import com.elypsoeed.martlett.common.testdata.TestDataProperties;
import com.elypsoeed.martlett.common.testdata.model.TestUser;
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
		testData.createRepository(testUser, "clone-no-auth");

		assertThatThrownBy(() -> cloneRepository(
			testUser.username(),
			"clone-no-auth",
			tempDir.resolve("repository")
		))
			.isInstanceOf(TransportException.class);
	}

	@Test
	void success(@TempDir Path tempDir) throws Exception {
		TestUser testUser = testData.createAuthedUser(testInfo);
		testData.createRepository(testUser, "clone-success");

		Path cloneDirectory = tempDir.resolve("repository");
		try (var git = testData.cloneRepository(testUser, testUser.username(), "clone-success", cloneDirectory)) {
			assertThat(Files.exists(cloneDirectory.resolve(".git"))).isTrue();
			assertThat(git.getRepository().getRemoteNames()).containsExactly("origin");
		}
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
