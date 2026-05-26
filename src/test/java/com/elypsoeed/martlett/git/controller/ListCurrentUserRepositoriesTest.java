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
public class ListCurrentUserRepositoriesTest {

	private final TestData testData;
	private final TestDataProperties testDataProperties;

	@Test
	void noAuth() {
		Response response = get();

		assertThat(response.statusCode()).isEqualTo(401);
	}

	@Test
	void success(TestInfo testInfo) {
		TestUser currentUser = testData.createAuthedUser(testInfo);
		TestUser anotherUser = testData.createAuthedUser(testInfo);

		testData.createRepositoryFromRequest(currentUser, new CreateRepositoryRequest()
			.name("private-repository")
			.visibility(RepositoryVisibility.PRIVATE));
		testData.createRepositoryFromRequest(currentUser, new CreateRepositoryRequest()
			.name("public-repository")
			.visibility(RepositoryVisibility.PUBLIC));
		testData.createPrivateRepository(anotherUser, "foreign-repository");

		Response response = getAuthenticated(currentUser.accessToken());

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.jsonPath().getList("name", String.class))
			.containsExactly("public-repository", "private-repository");
		assertThat(response.jsonPath().getList("fullName", String.class))
			.containsExactly(
				currentUser.username() + "/public-repository",
				currentUser.username() + "/private-repository"
			);
		assertThat(response.jsonPath().getList("ownerNickname", String.class))
			.containsOnly(currentUser.username());
	}

	private Response get() {
		return executeGet(request());
	}

	private Response getAuthenticated(String accessToken) {
		RequestSpecification request = request();
		request.auth().oauth2(accessToken);
		return executeGet(request);
	}

	private RequestSpecification request() {
		return given()
			.baseUri(testDataProperties.getApiBaseUrl())
			.port(testDataProperties.testPort())
			.contentType("application/json")
			.accept("application/json");
	}

	private Response executeGet(RequestSpecification request) {
		return request
			.when()
			.get("/api/repositories")
			.then()
			.extract()
			.response();
	}
}
