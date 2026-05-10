package com.elypsoeed.martlett.auth.controller;

import com.elypsoeed.martlett.IntegrationTest;
import com.elypsoeed.martlett.auth.model.Role;
import com.elypsoeed.martlett.common.testdata.TestData;
import com.elypsoeed.martlett.common.testdata.TestData.TestUser;
import com.elypsoeed.martlett.generated.model.RegisterUserRequest;
import com.fasterxml.jackson.databind.JsonNode;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
public class AuthControllerTest {

    @Autowired
    private TestData testData;

    @Test
    void registersUser() {
        RegisterUserRequest registerUserRequest = testData.newRegistrationRequest(getClass(), "success");
        Response response = testData.registerUserResponse(registerUserRequest);

        assertThat(response.statusCode()).isEqualTo(201);

        Response tokenResponse = testData.issueTokenResponse(registerUserRequest.getUsername(), registerUserRequest.getPassword());
        assertThat(tokenResponse.statusCode()).isEqualTo(200);

        TestUser testUser = testData.getAuthedUser(getClass());
        assertThat(testUser.roles()).containsExactly(Role.USER);
    }

    @Test
    void rejectsDuplicateUsernameOrNickname() {
        RegisterUserRequest registerUserRequest = testData.newRegistrationRequest(getClass(), "duplicate");
        Response firstResponse = testData.registerUserResponse(registerUserRequest);
        Response secondResponse = testData.registerUserResponse(registerUserRequest);

        assertThat(firstResponse.statusCode()).isEqualTo(201);
        assertThat(secondResponse.statusCode()).isEqualTo(409);
    }

    @Test
    void success() {
        TestUser registeredUser = testData.getAuthedUser(getClass());

        assertThat(registeredUser.accessToken()).isNotBlank();
        assertThat(registeredUser.refreshToken()).isNotBlank();
        assertThat(registeredUser.accessExpiresIn()).isEqualTo(3600);
        assertThat(registeredUser.refreshExpiresIn()).isEqualTo(1209600);
        assertThat(registeredUser.roles()).containsExactly(Role.USER);
    }

    @Test
    void adminHasAdminRole() {
        TestUser admin = testData.getAuthedAdmin();

        assertThat(admin.roles()).containsExactlyInAnyOrder(Role.USER, Role.ADMIN);
    }

    @Test
    void invalidPassword() {
        TestUser registeredUser = testData.getAuthedUser(getClass());
        Response response = testData.issueTokenResponse(registeredUser.username(), "invalid-password");

        assertThat(response.statusCode()).isEqualTo(401);
    }

    @Test
    void newTokenPair() {
        TestUser issuedUser = testData.getAuthedUser(getClass());

        Response response = testData.refreshTokenResponse(issuedUser.refreshToken());

        assertThat(response.statusCode()).isEqualTo(200);

        JsonNode json = response.as(JsonNode.class);
        assertThat(json.path("accessToken").asText()).isNotBlank();
        assertThat(json.path("refreshToken").asText()).isNotBlank();
        assertThat(json.path("accessToken").asText()).isNotEqualTo(issuedUser.accessToken());
        assertThat(json.path("refreshToken").asText()).isNotEqualTo(issuedUser.refreshToken());
    }

    @Test
    void swapTokens() {
        TestUser issuedUser = testData.getAuthedUser(getClass());

        Response response = testData.refreshTokenResponse(issuedUser.accessToken());

        assertThat(response.statusCode()).isEqualTo(401);
    }

    @Test
    void malformedToken() {
        Response response = testData.refreshTokenResponse("not-a-jwt");

        assertThat(response.statusCode()).isEqualTo(401);
    }
}
