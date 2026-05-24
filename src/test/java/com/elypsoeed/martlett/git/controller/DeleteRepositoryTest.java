package com.elypsoeed.martlett.git.controller;

import com.elypsoeed.martlett.IntegrationTest;
import com.elypsoeed.martlett.auth.repository.AuthUserRepository;
import com.elypsoeed.martlett.common.testdata.TestData;
import com.elypsoeed.martlett.common.testdata.TestDataProperties;
import com.elypsoeed.martlett.common.testdata.model.TestUser;
import com.elypsoeed.martlett.git.config.properties.GitStorageProperties;
import com.elypsoeed.martlett.git.entity.GitRepoEntity;
import com.elypsoeed.martlett.git.repository.GitRepoRepository;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.nio.file.Files;
import java.nio.file.Path;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
@RequiredArgsConstructor
public class DeleteRepositoryTest {

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
	void noAuth() {
		Response response = delete(null, "sample");

		assertThat(response.statusCode()).isEqualTo(401);
	}

	@Test
	void success() {
		TestUser testUser = testData.createAuthedUser(testInfo);
		Response createResponse = testData.createPrivateRepository(testUser, "repository-to-delete");

		assertThat(createResponse.statusCode()).isEqualTo(201);

		Long ownerId = authUserRepository.findByUsername(testUser.username())
			.orElseThrow()
			.getUserId();

		GitRepoEntity gitRepo = gitRepoRepository
			.findByNameAndOwnerId(createResponse.jsonPath().getString("name"), ownerId)
			.orElseThrow();

		Path repositoryPath = Path.of(gitStorageProperties.getRootPath())
			.toAbsolutePath()
			.normalize()
			.resolve(gitRepo.getStorageRelativePath());

		Response deleteResponse = delete(
			testUser.accessToken(),
			createResponse.jsonPath().getString("name")
		);

		assertThat(deleteResponse.statusCode()).isEqualTo(204);
		assertThat(gitRepoRepository.findByNameAndOwnerId(createResponse.jsonPath().getString("name"), ownerId)).isEmpty();
		assertThat(Files.exists(repositoryPath)).isFalse();
	}

	@Test
	void missingRepository() {
		TestUser testUser = testData.createAuthedUser(testInfo);
		Response response = delete(testUser.accessToken(), "missing-repository");

		assertThat(response.statusCode()).isEqualTo(404);
	}

	@Test
	void rejectsInvalidRepositoryName() {
		TestUser testUser = testData.createAuthedUser(testInfo);
		Response response = delete(testUser.accessToken(), "bad!name");

		assertThat(response.statusCode()).isEqualTo(400);
	}

	private RequestSpecification request() {
		return given()
			.baseUri(testDataProperties.getApiBaseUrl())
			.port(testDataProperties.testPort())
			.contentType("application/json")
			.accept("application/json");
	}

	private Response delete(String accessToken, String repositoryName) {
		RequestSpecification request = request();

		if (accessToken != null) {
			request.auth().oauth2(accessToken);
		}

		return request
			.when()
			.delete("/api/repositories/" + repositoryName)
			.then()
			.extract()
			.response();
	}
}
