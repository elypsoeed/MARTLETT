package com.elypsoeed.martlett.git.controller;

import com.elypsoeed.martlett.IntegrationTest;
import com.elypsoeed.martlett.common.testdata.TestData;
import com.elypsoeed.martlett.common.testdata.TestDataProperties;
import com.elypsoeed.martlett.common.testdata.model.TestUser;
import com.elypsoeed.martlett.generated.model.CreateRepositoryRequest;
import com.elypsoeed.martlett.generated.model.RepositoryVisibility;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
@RequiredArgsConstructor
public class GetRepositoryTest {

	private final TestData testData;
	private final TestDataProperties testDataProperties;

	@Test
	void noAuth(TestInfo testInfo) {
		TestUser owner = testData.createAuthedUser(testInfo);
		testData.createPrivateRepository(owner, "private-repository");

		Response response = get(owner.username());

		assertThat(response.statusCode()).isEqualTo(403);
	}

	@Test
	void noAccess(TestInfo testInfo) {
		TestUser owner = testData.createAuthedUser(testInfo);
		TestUser stranger = testData.createAuthedUser(testInfo);
		testData.createPrivateRepository(owner, "private-repository");

		Response response = getAuthed(stranger.accessToken(), owner.username(), "private-repository");

		assertThat(response.statusCode()).isEqualTo(403);
	}

	@Test
	void success(TestInfo testInfo) {
		TestUser owner = testData.createAuthedUser(testInfo);
		testData.createRepositoryFromRequest(owner, new CreateRepositoryRequest()
			.name("public-repository")
			.visibility(RepositoryVisibility.PUBLIC));

		Response response = getAuthed(owner.accessToken(), owner.username(), "public-repository");

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.jsonPath().getString("name")).isEqualTo("public-repository");
		assertThat(response.jsonPath().getString("ownerNickname")).isEqualTo(owner.username());
		assertThat(response.jsonPath().getString("fullName")).isEqualTo(owner.username() + "/public-repository");
		assertThat(response.jsonPath().getString("visibility")).isEqualTo("PUBLIC");
		assertThat(response.jsonPath().getString("createdTimestamp")).isNotBlank();
	}

	@Test
	void missingRepository(TestInfo testInfo) {
		TestUser owner = testData.createAuthedUser(testInfo);

		Response response = getAuthed(owner.accessToken(), owner.username(), "missing-repository");

		assertThat(response.statusCode()).isEqualTo(404);
	}

	private Response get(String username) {
		return executeGet(request(), username, "private-repository");
	}

	private Response getAuthed(String accessToken, String username, String repositoryName) {
		RequestSpecification request = request();
		request.auth().oauth2(accessToken);
		return executeGet(request, username, repositoryName);
	}

	private Response executeGet(RequestSpecification request, String username, String repositoryName) {
		return request
				.when()
				.get("/api/repositories/" + username + "/" + repositoryName)
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
