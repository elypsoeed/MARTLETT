package com.elypsoeed.martlett.git.controller;

import com.elypsoeed.martlett.IntegrationTest;
import com.elypsoeed.martlett.common.testdata.TestData;
import com.elypsoeed.martlett.common.testdata.TestData.TestUser;
import com.elypsoeed.martlett.common.testdata.TestDataProperties;
import com.elypsoeed.martlett.generated.model.CreateRepositoryRequest;
import com.elypsoeed.martlett.generated.model.RegisterUserRequest;
import com.elypsoeed.martlett.git.config.properties.GitStorageProperties;
import com.elypsoeed.martlett.git.entity.HostedRepositoryEntity;
import com.elypsoeed.martlett.git.repository.HostedRepositoryRepository;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
public class RepositoryControllerTest {

	@Autowired
	private TestData testData;

	@Autowired
	private TestDataProperties testDataProperties;

	@Autowired
	private GitStorageProperties gitStorageProperties;

	@Autowired
	private HostedRepositoryRepository hostedRepositoryRepository;

	@LocalServerPort
	private int port;

	@Test
	void noAuth() {
		Response response = request()
			.body(new CreateRepositoryRequest().name("sample"))
			.when()
			.post("/api/repositories")
			.then()
			.extract()
			.response();

		assertThat(response.statusCode()).isEqualTo(401);
	}

	@Test
	void success() throws IOException {
		TestUser testUser = testData.getAuthedUser(getClass());
		RegisterUserRequest registrationRequest = testData.newRegistrationRequest(getClass(), "shared");
		CreateRepositoryRequest createRepositoryRequest = new CreateRepositoryRequest().name("sample-repository");

		Response response = request()
			.auth().oauth2(testUser.accessToken())
			.body(createRepositoryRequest)
			.when()
			.post("/api/repositories")
			.then()
			.extract()
			.response();

		assertThat(response.statusCode()).isEqualTo(201);
		assertThat(response.jsonPath().getLong("id")).isPositive();
		assertThat(response.jsonPath().getString("name")).isEqualTo("sample-repository");
		assertThat(response.jsonPath().getString("ownerNickname")).isEqualTo(registrationRequest.getNickname());
		assertThat(response.jsonPath().getString("fullName"))
			.isEqualTo(registrationRequest.getNickname() + "/sample-repository");
		assertThat(response.jsonPath().getString("createdTimestamp")).isNotBlank();

		HostedRepositoryEntity hostedRepository = hostedRepositoryRepository
			.findByOwnerNicknameAndName(registrationRequest.getNickname(), "sample-repository")
			.orElseThrow();

		Path repositoryPath = Path.of(gitStorageProperties.getRootPath())
			.toAbsolutePath()
			.normalize()
			.resolve(hostedRepository.getStorageRelativePath());

		assertThat(Files.exists(repositoryPath.resolve("HEAD"))).isTrue();
		try (var repository = new FileRepositoryBuilder().setGitDir(repositoryPath.toFile()).build()) {
			assertThat(repository.isBare()).isTrue();
		}
	}

	@Test
	void duplicateRepositoryName() {
		TestUser testUser = testData.getAuthedUser(getClass());
		CreateRepositoryRequest createRepositoryRequest = new CreateRepositoryRequest().name("duplicate-repository");

		Response firstResponse = request()
			.auth().oauth2(testUser.accessToken())
			.body(createRepositoryRequest)
			.when()
			.post("/api/repositories")
			.then()
			.extract()
			.response();

		Response secondResponse = request()
			.auth().oauth2(testUser.accessToken())
			.body(createRepositoryRequest)
			.when()
			.post("/api/repositories")
			.then()
			.extract()
			.response();

		assertThat(firstResponse.statusCode()).isEqualTo(201);
		assertThat(secondResponse.statusCode()).isEqualTo(409);
	}

	private RequestSpecification request() {
		return given()
			.baseUri(testDataProperties.getApi().getBaseUrl())
			.port(port)
			.contentType("application/json")
			.accept("application/json");
	}
}
