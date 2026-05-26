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
public class GetRepositoryTreeTest {

	private final TestData testData;
	private final TestDataProperties testDataProperties;

	@Test
	void noAccess(TestInfo testInfo) {
		TestUser owner = testData.createAuthedUser(testInfo);
		TestUser stranger = testData.createAuthedUser(testInfo);
		testData.createPrivateRepository(owner, "tree-repository");

		Response response = getAuthenticated(stranger.accessToken(), owner.username(), null);

		assertThat(response.statusCode()).isEqualTo(403);
	}

	@Test
	void success(TestInfo testInfo, @TempDir Path tempDir) throws Exception {
		TestUser owner = testData.createAuthedUser(testInfo);
		testData.createPrivateRepository(owner, "tree-repository");
		testData.pushFiles(
			owner,
			owner.username(),
			"tree-repository",
			tempDir.resolve("repository"),
			Map.of(
				"README.md", "hello",
				"src/App.java", "class App {}"
			)
		);

		Response response = getAuthenticated(owner.accessToken(), owner.username(), null);

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.jsonPath().getString("ref")).isEqualTo("master");
		assertThat(response.jsonPath().getString("commit.sha")).hasSize(40);
		assertThat(response.jsonPath().getList("entries.name", String.class)).containsExactly("src", "README.md");
		assertThat(response.jsonPath().getList("entries.path", String.class)).containsExactly("src", "README.md");
		assertThat(response.jsonPath().getList("entries.type", String.class)).containsExactly("DIRECTORY", "FILE");
		assertThat(response.jsonPath().getList("entries.size", Integer.class)).containsExactly(null, "hello".length());
	}

	@Test
	void missingPath(TestInfo testInfo, @TempDir Path tempDir) throws Exception {
		TestUser owner = testData.createAuthedUser(testInfo);
		testData.createPrivateRepository(owner, "tree-repository");
		testData.pushFiles(
			owner,
			owner.username(),
			"tree-repository",
			tempDir.resolve("repository"),
			Map.of("README.md", "hello")
		);

		Response response = getAuthenticated(owner.accessToken(), owner.username(), "missing");

		assertThat(response.statusCode()).isEqualTo(404);
	}

	private Response getAuthenticated(
		String accessToken,
		String username,
		String path
	) {
		RequestSpecification request = request();
		request.auth().oauth2(accessToken);
		return executeGet(request, username, path);
	}

	private Response executeGet(
		RequestSpecification request,
		String username,
		String path
	) {
		RequestSpecification result = request.queryParam("ref", "master");
		if (path != null) {
			result.queryParam("path", path);
		}

		return result
			.when()
			.get("/api/repositories/" + username + "/" + "tree-repository" + "/tree")
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
