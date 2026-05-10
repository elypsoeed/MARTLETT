package com.elypsoeed.martlett.core.controller;

import com.elypsoeed.martlett.IntegrationTest;
import com.elypsoeed.martlett.auth.model.Role;
import com.elypsoeed.martlett.common.testdata.TestData;
import com.elypsoeed.martlett.common.testdata.TestData.TestUser;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;
import static io.restassured.RestAssured.given;

@IntegrationTest
public class HiControllerTest {

    @Value("${test.api.base-url}")
    private String baseUrl;

    @Autowired
    private TestData testData;

    @LocalServerPort
    private int port;

    @Test
    void noAuth() {
        Response response = request()
                .when()
                .post("/api/hi")
                .then()
                .extract()
                .response();

        assertThat(response.statusCode()).isEqualTo(401);
    }

    @Test
    void success() {
        TestUser testUser = testData.getAuthedUser(getClass());

        assertThat(testUser.roles()).containsExactly(Role.USER);

        Response response = request()
                .auth().oauth2(testUser.accessToken())
                .when()
                .post("/api/hi")
                .then()
                .extract()
                .response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("message")).isEqualTo("Hi, Sasha");
    }

    @Test
    void adminCanAccessUserEndpoint() {
        TestUser admin = testData.getAuthedAdmin();

        assertThat(admin.roles()).containsExactlyInAnyOrder(Role.USER, Role.ADMIN);

        Response response = request()
                .auth().oauth2(admin.accessToken())
                .when()
                .post("/api/hi")
                .then()
                .extract()
                .response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("message")).isEqualTo("Hi, Sasha");
    }

    @Test
    void refreshTokenIssuesNewAccessToken() {
        TestUser issuedTokens = testData.getAuthedUser(getClass());
        TestUser refreshedTokens = testData.refreshToken(issuedTokens);

        assertThat(refreshedTokens.accessToken()).isNotEqualTo(issuedTokens.accessToken());
        assertThat(refreshedTokens.refreshToken()).isNotEqualTo(issuedTokens.refreshToken());

        Response hiResponse = request()
                .auth().oauth2(refreshedTokens.accessToken())
                .when()
                .post("/api/hi")
                .then()
                .extract()
                .response();

        assertThat(hiResponse.statusCode()).isEqualTo(200);
        assertThat(hiResponse.jsonPath().getString("message")).isEqualTo("Hi, Sasha");
    }

    private RequestSpecification request() {
        return given()
                .baseUri(baseUrl)
                .port(port)
                .contentType("text/plain")
                .accept("application/json")
                .body("Sasha");
    }
}
