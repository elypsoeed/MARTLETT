package com.elypsoeed.martlett.git.controller;

import com.elypsoeed.martlett.IntegrationTest;
import com.elypsoeed.martlett.common.testdata.TestData;
import com.elypsoeed.martlett.common.testdata.TestDataProperties;
import com.elypsoeed.martlett.common.testdata.model.TestUser;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
@RequiredArgsConstructor
public class GetRepositoryReadmeTest {

	private static final String REPOSITORY_NAME = "readme-repository";

	private final TestData testData;
	private final TestDataProperties testDataProperties;

	@Test
	void noAuth(TestInfo testInfo) {
		TestUser owner = testData.createAuthedUser(testInfo);
		testData.createPrivateRepository(owner, REPOSITORY_NAME);

		Response response = get(owner.username(), REPOSITORY_NAME);

		assertThat(response.statusCode()).isEqualTo(401);
	}

	@Test
	void noAccess(TestInfo testInfo) {
		TestUser owner = testData.createAuthedUser(testInfo);
		TestUser stranger = testData.createAuthedUser(testInfo);
		testData.createPrivateRepository(owner, REPOSITORY_NAME);

		Response response = getAuthenticated(stranger.accessToken(), owner.username(), REPOSITORY_NAME);

		assertThat(response.statusCode()).isEqualTo(403);
	}

	@Test
	void success(TestInfo testInfo, @TempDir Path tempDir) throws Exception {
		TestUser owner = testData.createAuthedUser(testInfo);
		testData.createPrivateRepository(owner, REPOSITORY_NAME);
		testData.pushFiles(
			owner,
			owner.username(),
			REPOSITORY_NAME,
			tempDir.resolve("repository"),
			Map.of("README.md", "# Project")
		);

		Response response = getAuthenticated(owner.accessToken(), owner.username(), REPOSITORY_NAME);

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.jsonPath().getString("name")).isEqualTo("README.md");
		assertThat(response.jsonPath().getString("content")).isEqualTo("# Project");
	}

	@Test
	void missingReadme(TestInfo testInfo, @TempDir Path tempDir) throws Exception {
		TestUser owner = testData.createAuthedUser(testInfo);
		testData.createPrivateRepository(owner, REPOSITORY_NAME);
		testData.pushFiles(
			owner,
			owner.username(),
			REPOSITORY_NAME,
			tempDir.resolve("repository"),
			Map.of("src/App.java", "class App {}")
		);

		Response response = getAuthenticated(owner.accessToken(), owner.username(), REPOSITORY_NAME);

		assertThat(response.statusCode()).isEqualTo(404);
	}

	@Test
	void missingRepository(TestInfo testInfo) {
		TestUser owner = testData.createAuthedUser(testInfo);

		Response response = getAuthenticated(owner.accessToken(), owner.username(), REPOSITORY_NAME);

		assertThat(response.statusCode()).isEqualTo(404);
	}

	private Response get(String username, String repositoryName) {
		return executeGet(request(), username, repositoryName);
	}

	private Response getAuthenticated(String accessToken, String username, String repositoryName) {
		return executeGet(
			request().auth().oauth2(accessToken),
			username,
			repositoryName
		);
	}

	private Response executeGet(RequestSpecification request, String username, String repositoryName) {
		return request
			.queryParam("ref", "master")
			.when()
			.get("/api/repositories/" + username + "/" + repositoryName + "/readme")
			.then()
			.extract()
			.response();
	}

	private RequestSpecification request() {
		return given()
			.baseUri(testDataProperties.getApiBaseUrl())
			.port(testDataProperties.testPort())
			.contentType("application/json")
			.accept("application/json");
	}
}
