package com.elypsoeed.martlett.auth.controller;

import com.elypsoeed.martlett.IntegrationTest;
import com.elypsoeed.martlett.common.testdata.TestDataProperties;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
@RequiredArgsConstructor
public class CorsConfigurationTest {

	private static final String ALLOWED_ORIGIN = "http://localhost:5173";
	private static final String DISALLOWED_ORIGIN = "http://lavrent.dog";

	private final TestDataProperties testDataProperties;


	@Test
	void success() {
		Response response = preflight(ALLOWED_ORIGIN, "PATCH", "/api/me");

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.getHeader("Access-Control-Allow-Origin")).isEqualTo(ALLOWED_ORIGIN);
		assertThat(response.getHeader("Access-Control-Allow-Methods")).contains("PATCH");
		assertThat(response.getHeader("Access-Control-Allow-Headers")).contains("Authorization");
	}

	@Test
	void avatarUpdateSuccess() {
		Response response = preflight(ALLOWED_ORIGIN, "PUT", "/api/me/avatar");

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.getHeader("Access-Control-Allow-Origin")).isEqualTo(ALLOWED_ORIGIN);
		assertThat(response.getHeader("Access-Control-Allow-Methods")).contains("PUT");
		assertThat(response.getHeader("Access-Control-Allow-Headers")).contains("Content-Type");
	}

	@Test
	void noAccess() {
		Response response = preflight(DISALLOWED_ORIGIN, "PATCH", "/api/me");

		assertThat(response.statusCode()).isEqualTo(403);
	}

	private Response preflight(String origin, String method, String path) {
		return given()
			.baseUri(testDataProperties.getApiBaseUrl())
			.port(testDataProperties.testPort())
			.accept("application/json")
			.header("Origin", origin)
			.header("Access-Control-Request-Method", method)
			.header("Access-Control-Request-Headers", "Authorization, Content-Type")
			.when()
			.options(path)
			.then()
			.extract()
			.response();
	}
}
