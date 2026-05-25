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
public class GetRepositoryRawBlobTest {

	private static final String REPOSITORY_NAME = "raw-repository";

	private final TestData testData;
	private final TestDataProperties testDataProperties;

	@Test
	void noAuth(TestInfo testInfo) {
		TestUser owner = testData.createAuthedUser(testInfo);
		testData.createPrivateRepository(owner, REPOSITORY_NAME);

		Response response = get(owner.username(), REPOSITORY_NAME, "README.md");

		assertThat(response.statusCode()).isEqualTo(401);
	}

	@Test
	void noAccess(TestInfo testInfo) {
		TestUser owner = testData.createAuthedUser(testInfo);
		TestUser stranger = testData.createAuthedUser(testInfo);
		testData.createPrivateRepository(owner, REPOSITORY_NAME);

		Response response = getAuthenticated(stranger.accessToken(), owner.username(), REPOSITORY_NAME, "README.md");

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
			Map.of("README.md", "hello")
		);

		Response response = getAuthenticated(owner.accessToken(), owner.username(), REPOSITORY_NAME, "README.md");

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.asByteArray()).containsExactly("hello".getBytes());
	}

	@Test
	void missingPath(TestInfo testInfo, @TempDir Path tempDir) throws Exception {
		TestUser owner = testData.createAuthedUser(testInfo);
		testData.createPrivateRepository(owner, REPOSITORY_NAME);
		testData.pushFiles(
			owner,
			owner.username(),
			REPOSITORY_NAME,
			tempDir.resolve("repository"),
			Map.of("README.md", "hello")
		);

		Response response = getAuthenticated(owner.accessToken(), owner.username(), REPOSITORY_NAME, "missing.txt");

		assertThat(response.statusCode()).isEqualTo(404);
	}

	private Response get(String username, String repositoryName, String path) {
		return executeGet(request(), username, repositoryName, path);
	}

	private Response getAuthenticated(String accessToken, String username, String repositoryName, String path) {
		return executeGet(
			request().auth().oauth2(accessToken),
			username,
			repositoryName,
			path
		);
	}

	private Response executeGet(RequestSpecification request, String username, String repositoryName, String path) {
		return request
			.queryParam("ref", "master")
			.queryParam("path", path)
			.when()
			.get("/api/repositories/" + username + "/" + repositoryName + "/blob/raw")
			.then()
			.extract()
			.response();
	}

	private RequestSpecification request() {
		return given()
			.baseUri(testDataProperties.getApiBaseUrl())
			.port(testDataProperties.testPort());
	}
}
