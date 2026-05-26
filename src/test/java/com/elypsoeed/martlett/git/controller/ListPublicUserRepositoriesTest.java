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
public class ListPublicUserRepositoriesTest {

	private final TestData testData;
	private final TestDataProperties testDataProperties;

	@Test
	void success(TestInfo testInfo) {
		TestUser owner = testData.createAuthedUser(testInfo);
		testData.createRepositoryFromRequest(owner, new CreateRepositoryRequest()
			.name("public-repository")
			.visibility(RepositoryVisibility.PUBLIC));
		testData.createPrivateRepository(owner, "private-repository");

		Response response = get(owner.username());

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.jsonPath().getList("name", String.class)).containsExactly("public-repository");
		assertThat(response.jsonPath().getList("ownerNickname", String.class)).containsOnly(owner.username());
		assertThat(response.jsonPath().getList("fullName", String.class))
			.containsExactly(owner.username() + "/public-repository");
	}

	@Test
	void missingUser() {
		Response response = get("missing-user");

		assertThat(response.statusCode()).isEqualTo(404);
	}

	private Response get(String username) {
		return request()
			.when()
			.get("/api/users/" + username + "/repositories")
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
