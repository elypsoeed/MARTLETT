package com.elypsoeed.martlett.git.controller;

import com.elypsoeed.martlett.IntegrationTest;
import com.elypsoeed.martlett.auth.repository.AuthUserRepository;
import com.elypsoeed.martlett.common.testdata.TestData;
import com.elypsoeed.martlett.common.testdata.TestDataProperties;
import com.elypsoeed.martlett.common.testdata.model.TestUser;
import com.elypsoeed.martlett.generated.model.CreateRepositoryRequest;
import com.elypsoeed.martlett.git.config.properties.GitStorageProperties;
import com.elypsoeed.martlett.git.entity.GitRepositoryEntity;
import com.elypsoeed.martlett.git.repository.GitRepositoryRepository;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
@RequiredArgsConstructor
public class CreateRepositoryTest {

	private final TestData testData;
	private final TestDataProperties testDataProperties;
	private final GitStorageProperties gitStorageProperties;
	private final GitRepositoryRepository gitRepositoryRepository;
	private final AuthUserRepository authUserRepository;
	private TestInfo testInfo;

	@BeforeEach
	void setUp(TestInfo testInfo) {
		this.testInfo = testInfo;
	}

	@Test
	void noAuth() {
		Response response = post(null, new CreateRepositoryRequest().name("sample"));

		assertThat(response.statusCode()).isEqualTo(401);
	}

	@Test
	void success() throws IOException {
		TestUser testUser = testData.createAuthedUser(testInfo);
		Response response = post(testUser.accessToken(), new CreateRepositoryRequest().name("sample-repository"));

		assertThat(response.statusCode()).isEqualTo(201);
		assertThat(response.jsonPath().getLong("id")).isPositive();
		assertThat(response.jsonPath().getString("name")).isEqualTo("sample-repository");
		assertThat(response.jsonPath().getString("fullName"))
			.isEqualTo(testUser.username() + "/sample-repository");
		assertThat(response.jsonPath().getString("createdTimestamp")).isNotBlank();

		Long ownerId = authUserRepository.findByUsername(testUser.username())
			.orElseThrow()
			.getUserId();

		GitRepositoryEntity gitRepository = gitRepositoryRepository
			.findByNameAndOwnerId(response.jsonPath().getString("name"), ownerId)
			.orElseThrow();

		Path repositoryPath = Path.of(gitStorageProperties.getRootPath())
			.toAbsolutePath()
			.normalize()
			.resolve(gitRepository.getStorageRelativePath());

		assertThat(Files.exists(repositoryPath.resolve("HEAD"))).isTrue();
		try (var repository = new FileRepositoryBuilder().setGitDir(repositoryPath.toFile()).build()) {
			assertThat(repository.isBare()).isTrue();
		}
	}

	@Test
	void rejectsDuplicateRepositoryName() {
		TestUser testUser = testData.createAuthedUser(testInfo);
		CreateRepositoryRequest createRepositoryRequest = new CreateRepositoryRequest().name("duplicate-repository");

		Response firstResponse = post(testUser.accessToken(), createRepositoryRequest);
		Response secondResponse = post(testUser.accessToken(), createRepositoryRequest);

		assertThat(firstResponse.statusCode()).isEqualTo(201);
		assertThat(secondResponse.statusCode()).isEqualTo(409);
	}

	private RequestSpecification request() {
		return given()
			.baseUri(testDataProperties.getApiBaseUrl())
			.port(testDataProperties.testPort())
			.contentType("application/json")
			.accept("application/json");
	}

	private Response post(String accessToken, Object body) {
		RequestSpecification request = request().body(body);

		if (accessToken != null) {
			request.auth().oauth2(accessToken);
		}

		return request
			.when()
			.post("/api/repositories")
			.then()
			.extract()
			.response();
	}
}
