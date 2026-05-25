package com.elypsoeed.martlett.user.controller;

import com.elypsoeed.martlett.IntegrationTest;
import com.elypsoeed.martlett.common.testdata.TestData;
import com.elypsoeed.martlett.common.testdata.TestDataProperties;
import com.elypsoeed.martlett.common.testdata.model.TestUser;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
@RequiredArgsConstructor
public class GetPublicUserProfileTest {

	private final TestData testData;
	private final TestDataProperties testDataProperties;

	@Test
	void success(TestInfo testInfo) {
		TestUser testUser = testData.createAuthedUser(testInfo);

		Response response = get(testUser.username());

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.jsonPath().getString("username")).isEqualTo(testUser.username());
		assertThat(response.jsonPath().getString("firstName")).isEqualTo("Test");
		assertThat(response.jsonPath().getString("lastName")).isEqualTo("User");
		assertThat(response.jsonPath().getString("city")).isEqualTo("Moscow");
		assertThat(response.jsonPath().getString("registrationTimestamp")).isNotBlank();
	}

	@Test
	void missingUser() {
		Response response = get("missing-user");

		assertThat(response.statusCode()).isEqualTo(404);
	}

	private Response get(String username) {
		return request()
			.when()
			.get("/api/users/" + username)
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
