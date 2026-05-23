package com.elypsoeed.martlett.git.controller;

import com.elypsoeed.martlett.IntegrationTest;
import com.elypsoeed.martlett.auth.repository.AuthUserRepository;
import com.elypsoeed.martlett.common.testdata.TestData;
import com.elypsoeed.martlett.common.testdata.model.TestUser;
import com.elypsoeed.martlett.common.testdata.TestDataProperties;
import com.elypsoeed.martlett.generated.model.CreateRepositoryRequest;
import com.elypsoeed.martlett.git.config.properties.GitStorageProperties;
import com.elypsoeed.martlett.git.entity.HostedRepositoryEntity;
import com.elypsoeed.martlett.git.repository.HostedRepositoryRepository;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.io.IOException;
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
	private final HostedRepositoryRepository hostedRepositoryRepository;
	private final AuthUserRepository authUserRepository;

	@Test
	void noAuth() {
		Response response = delete(null, "sample");

		assertThat(response.statusCode()).isEqualTo(401);
	}

	@Test
	void success(TestInfo testInfo) throws IOException {
		TestUser testUser = testData.createAuthedUser(testInfo);
		Response createResponse = post(testUser.accessToken(), new CreateRepositoryRequest().name("repository-to-delete"));

		assertThat(createResponse.statusCode()).isEqualTo(201);

		Long ownerId = authUserRepository.findByUsername(testUser.username())
			.orElseThrow()
			.getUserId();

		HostedRepositoryEntity hostedRepository = hostedRepositoryRepository
			.findByNameAndOwnerId(createResponse.jsonPath().getString("name"), ownerId)
			.orElseThrow();

		Path repositoryPath = Path.of(gitStorageProperties.getRootPath())
			.toAbsolutePath()
			.normalize()
			.resolve(hostedRepository.getStorageRelativePath());

		Response deleteResponse = delete(
			testUser.accessToken(),
			createResponse.jsonPath().getString("name")
		);

		assertThat(deleteResponse.statusCode()).isEqualTo(204);
		assertThat(hostedRepositoryRepository.findByNameAndOwnerId(createResponse.jsonPath().getString("name"), ownerId)).isEmpty();
		assertThat(Files.exists(repositoryPath)).isFalse();
	}

	@Test
	void missingRepository(TestInfo testInfo) {
		TestUser testUser = testData.createAuthedUser(testInfo);
		Response response = delete(testUser.accessToken(), "missing-repository");

		assertThat(response.statusCode()).isEqualTo(404);
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

    private Response delete(String accessToken, String repositoryName) {
        RequestSpecification request = given()
                .baseUri(testDataProperties.getApiBaseUrl())
                .port(testDataProperties.testPort())
                .contentType("application/json")
                .accept("application/json")
                .log().all();  // ← Добавить логирование

        if (accessToken != null) {
            request = request.auth().oauth2(accessToken);
        }

        return request
                .when()
                .delete("/api/repositories/" + repositoryName)
                .then()
                .log().all()  // ← И тут
                .extract()
                .response();
    }
}
