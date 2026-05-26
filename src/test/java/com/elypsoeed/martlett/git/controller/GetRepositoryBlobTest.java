package com.elypsoeed.martlett.git.controller;

import com.elypsoeed.martlett.IntegrationTest;
import com.elypsoeed.martlett.common.testdata.TestData;
import com.elypsoeed.martlett.common.testdata.TestDataProperties;
import com.elypsoeed.martlett.common.testdata.model.TestUser;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
@RequiredArgsConstructor
public class GetRepositoryBlobTest {

	private final TestData testData;
	private final TestDataProperties testDataProperties;
    private TestInfo testInfo;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        this.testInfo = testInfo;
    }

	@Test
	void success(@TempDir Path tempDir) throws Exception {
		TestUser owner = testData.createAuthedUser(testInfo);
		testData.createPrivateRepository(owner, "content-repository");
		testData.pushFiles(
			owner,
			owner.username(),
			"content-repository",
			tempDir.resolve("repository"),
			Map.of("src/App.java", "class App {}")
		);

		Response response = getAuthenticated(owner.accessToken(), owner.username(), "content-repository", "src/App.java");

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.jsonPath().getString("name")).isEqualTo("App.java");
		assertThat(response.jsonPath().getString("path")).isEqualTo("src/App.java");
		assertThat(response.jsonPath().getString("ref")).isEqualTo("master");
		assertThat(response.jsonPath().getString("commit.sha")).hasSize(40);
		assertThat(response.jsonPath().getBoolean("binary")).isFalse();
		assertThat(response.jsonPath().getString("content")).isEqualTo("class App {}");
		assertThat(response.jsonPath().getLong("size")).isEqualTo("class App {}".length());
	}

	@Test
	void missingPath(@TempDir Path tempDir) throws Exception {
		TestUser owner = testData.createAuthedUser(testInfo);
		testData.createPrivateRepository(owner, "missing-blob-repository");
		testData.pushFiles(
			owner,
			owner.username(),
			"missing-blob-repository",
			tempDir.resolve("repository"),
			Map.of("README.md", "hello")
		);

		Response response = getAuthenticated(
			owner.accessToken(),
			owner.username(),
			"missing-blob-repository",
                "missing.txt"
		);

		assertThat(response.statusCode()).isEqualTo(404);
	}

	private Response getAuthenticated(
		String accessToken,
		String username,
		String repositoryName,
        String path
	) {
		RequestSpecification request = request();
		request.auth().oauth2(accessToken);
		return executeGet(request, username, repositoryName, path);
	}

	private Response executeGet(
		RequestSpecification request,
		String username,
		String repositoryName,
        String path
	) {
		return request
			.queryParam("ref", "master")
			.queryParam("path", path)
			.when()
			.get("/api/repositories/" + username + "/" + repositoryName + "/blob")
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
