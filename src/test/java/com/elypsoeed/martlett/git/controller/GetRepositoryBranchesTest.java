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
public class GetRepositoryBranchesTest {

	private final TestData testData;
	private final TestDataProperties testDataProperties;

	@Test
	void noAuth(TestInfo testInfo) {
		TestUser owner = testData.createAuthedUser(testInfo);
		testData.createPrivateRepository(owner, "branches-repository");

		Response response = get(owner.username());

		assertThat(response.statusCode()).isEqualTo(401);
	}

	@Test
	void success(TestInfo testInfo, @TempDir Path tempDir) throws Exception {
		TestUser owner = testData.createAuthedUser(testInfo);
		testData.createPrivateRepository(owner, "branches-repository");
		testData.pushFiles(
			owner,
			owner.username(),
			"branches-repository",
			tempDir.resolve("repository"),
			Map.of("README.md", "hello")
		);

		Response response = getAuthenticated(owner.accessToken(), owner.username());

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.jsonPath().getList("name", String.class)).containsExactly("master");
		assertThat(response.jsonPath().getList("defaultBranch", Boolean.class)).containsExactly(true);
	}

	private Response get(String username) {
		return executeGet(request(), username);
	}

	private Response getAuthenticated(String accessToken, String username) {
		RequestSpecification request = request();
		request.auth().oauth2(accessToken);
		return executeGet(request, username);
	}

	private Response executeGet(RequestSpecification request, String username) {
		return request
			.when()
			.get("/api/repositories/" + username + "/" + "branches-repository" + "/branches")
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
