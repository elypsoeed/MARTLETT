package com.elypsoeed.martlett.auth.controller;

import com.elypsoeed.martlett.IntegrationTest;
import com.elypsoeed.martlett.auth.config.JwtDecoderFactory;
import com.elypsoeed.martlett.auth.model.Role;
import com.elypsoeed.martlett.common.testdata.TestDataProperties;
import com.elypsoeed.martlett.common.testdata.model.TestUser;
import com.elypsoeed.martlett.generated.model.RefreshTokenRequest;
import com.elypsoeed.martlett.generated.model.RegisterUserRequest;
import com.elypsoeed.martlett.generated.model.TokenRequest;
import com.elypsoeed.martlett.generated.model.TokenResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
@RequiredArgsConstructor
public class AuthControllerTest {

    private final JwtDecoderFactory jwtDecoderFactory;
    private final TestDataProperties testDataProperties;

    @Test
    void success(TestInfo testInfo) {
        TestUser registeredUser = createAuthedUser(testInfo);

        assertThat(registeredUser.accessToken()).isNotBlank();
        assertThat(registeredUser.refreshToken()).isNotBlank();
        assertThat(registeredUser.accessExpiresIn()).isEqualTo(3600);
        assertThat(registeredUser.refreshExpiresIn()).isEqualTo(1209600);
        assertThat(registeredUser.roles()).containsExactly(Role.USER);
    }

    @Test
    void duplicatedNickname(TestInfo testInfo) {
        RegisterUserRequest registerUserRequest = newRegistrationRequest(testInfo);
        Response firstResponse = sendRegisterUserRequest(registerUserRequest);
        Response secondResponse = sendRegisterUserRequest(registerUserRequest);

        assertThat(firstResponse.statusCode()).isEqualTo(201);
        assertThat(secondResponse.statusCode()).isEqualTo(409);
    }

    @Test
    void adminHasAdminRole() {
        TokenResponse tokenResponse = issueToken(
                testDataProperties.getAdminUsername(),
                testDataProperties.getAdminPassword()
        );
        TestUser admin = toTestUser(
                testDataProperties.getAdminUsername(),
                testDataProperties.getAdminPassword(),
                tokenResponse
        );

        assertThat(admin.roles()).containsExactlyInAnyOrder(Role.USER, Role.ADMIN);
    }

    @Test
    void invalidPassword(TestInfo testInfo) {
        RegisterUserRequest registerUserRequest = registerUser(testInfo);
        Response response = sendIssueTokenRequest(registerUserRequest.getUsername(), "invalid-password");

        assertThat(response.statusCode()).isEqualTo(401);
    }

    @Test
    void newTokenPair(TestInfo testInfo) {
        TestUser issuedUser = createAuthedUser(testInfo);

        TokenResponse tokenResponse = refreshToken(issuedUser.refreshToken());

        assertThat(tokenResponse.getAccessToken()).isNotBlank();
        assertThat(tokenResponse.getRefreshToken()).isNotBlank();
        assertThat(tokenResponse.getAccessToken()).isNotEqualTo(issuedUser.accessToken());
        assertThat(tokenResponse.getRefreshToken()).isNotEqualTo(issuedUser.refreshToken());
    }

    @Test
    void swapTokens(TestInfo testInfo) {
        TestUser issuedUser = createAuthedUser(testInfo);

        Response response = sendRefreshTokenRequest(issuedUser.accessToken());

        assertThat(response.statusCode()).isEqualTo(401);
    }

    @Test
    void malformedToken() {
        Response response = sendRefreshTokenRequest("not-a-jwt");

        assertThat(response.statusCode()).isEqualTo(401);
    }

    private Response sendRegisterUserRequest(RegisterUserRequest registerUserRequest) {
        return post("/api/auth/register", registerUserRequest);
    }

    private Response sendIssueTokenRequest(String username, String password) {
        return post("/api/auth/token",
                new TokenRequest()
                        .username(username)
                        .password(password)
        );
    }

    private TokenResponse issueToken(String username, String password) {
        Response response = sendIssueTokenRequest(username, password);

        assertThat(response.statusCode()).isEqualTo(200);

        return response.as(TokenResponse.class);
    }

    private Response sendRefreshTokenRequest(String refreshToken) {
        return post("/api/auth/refresh",
                new RefreshTokenRequest()
                        .refreshToken(refreshToken)
        );
    }

    private TokenResponse refreshToken(String refreshToken) {
        Response response = sendRefreshTokenRequest(refreshToken);

        assertThat(response.statusCode()).isEqualTo(200);

        return response.as(TokenResponse.class);
    }

    private RegisterUserRequest newRegistrationRequest(TestInfo testInfo) {
        String base = methodPrefix(testInfo) + "-" + UUID.randomUUID().toString().substring(0, 8);

        return new RegisterUserRequest()
                .firstName("Test")
                .lastName("User")
                .username(base + "-username")
                .password(base + "-password")
                .city("Moscow");
    }

    private RegisterUserRequest registerUser(TestInfo testInfo) {
        RegisterUserRequest registerUserRequest = newRegistrationRequest(testInfo);
        Response response = sendRegisterUserRequest(registerUserRequest);

        assertThat(response.statusCode()).isEqualTo(201);

        return registerUserRequest;
    }

    private TestUser createAuthedUser(TestInfo testInfo) {
        RegisterUserRequest registerUserRequest = registerUser(testInfo);
        TokenResponse tokenResponse = issueToken(registerUserRequest.getUsername(), registerUserRequest.getPassword());

        return toTestUser(registerUserRequest.getUsername(), registerUserRequest.getPassword(), tokenResponse);
    }

    private Response post(String path, Object body) {
        return request()
                .body(body)
                .when()
                .post(path)
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

    private TestUser toTestUser(String username, String password, TokenResponse tokenResponse) {
        Jwt accessJwt = jwtDecoderFactory.createAccessTokenDecoder().decode(tokenResponse.getAccessToken());

        return new TestUser(
                username,
                password,
                tokenResponse.getAccessToken(),
                tokenResponse.getRefreshToken(),
                tokenResponse.getAccessExpiresIn(),
                tokenResponse.getRefreshExpiresIn(),
                roles(accessJwt)
        );
    }

    private String methodPrefix(TestInfo testInfo) {
        String methodName = testInfo.getTestMethod()
                .orElseThrow()
                .getName()
                .toLowerCase();
        return methodName.length() > 20 ? methodName.substring(0, 20) : methodName;
    }

    private List<Role> roles(Jwt jwt) {
        return jwt.getClaimAsStringList("roles").stream()
                .map(Role::valueOf)
                .toList();
    }
}
