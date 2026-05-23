package com.elypsoeed.martlett.common.testdata;

import com.elypsoeed.martlett.auth.config.JwtDecoderFactory;
import com.elypsoeed.martlett.auth.model.Role;
import com.elypsoeed.martlett.common.testdata.model.TestUser;
import com.elypsoeed.martlett.common.testdata.model.UserCredentials;
import com.elypsoeed.martlett.generated.model.RegisterUserRequest;
import com.elypsoeed.martlett.generated.model.TokenRequest;
import com.elypsoeed.martlett.generated.model.TokenResponse;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.TestInfo;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;

import static io.restassured.RestAssured.given;

@Component
@RequiredArgsConstructor
public class TestData {

    private final JwtDecoderFactory jwtDecoderFactory;
    private final TestDataProperties testDataProperties;

    public TestUser createAuthedUser(TestInfo testInfo) {
        UserCredentials credentials = createUserCredentials(testInfo);
        TokenResponse tokenResponse = issueToken(credentials.username(), credentials.password());

        return toLoggedInUser(credentials.username(), tokenResponse);
    }

    public TestUser getAdminUser() {
        TokenResponse tokenResponse = issueToken(
                testDataProperties.getAdminUsername(),
                testDataProperties.getAdminPassword()
        );

        return toLoggedInUser(testDataProperties.getAdminUsername(), tokenResponse);
    }

    private void sendRegisterUserRequest(RegisterUserRequest registerUserRequest) {
        given()
                .baseUri(testDataProperties.getApiBaseUrl())
                .port(testDataProperties.testPort())
                .contentType("application/json")
                .accept("application/json")
                .body(registerUserRequest)
                .when()
                .post("/api/auth/register")
                .then()
                .extract()
                .response();
    }

    private Response sendIssueTokenRequest(String username, String password) {
        return given()
                .baseUri(testDataProperties.getApiBaseUrl())
                .port(testDataProperties.testPort())
                .contentType("application/json")
                .accept("application/json")
                .body(new TokenRequest()
                        .username(username)
                        .password(password))
                .when()
                .post("/api/auth/token")
                .then()
                .extract()
                .response();
    }

    private TokenResponse issueToken(String username, String password) {
        Response response = sendIssueTokenRequest(username, password);

        if (response.statusCode() != 200) {
            throw new IllegalStateException("Failed to issue token for test user, status: " + response.statusCode());
        }

        return response.as(TokenResponse.class);
    }

    private TestUser toLoggedInUser(String username, TokenResponse tokenResponse) {
        Jwt accessJwt = jwtDecoderFactory.createAccessTokenDecoder().decode(tokenResponse.getAccessToken());

        return new TestUser(
                username,
                tokenResponse.getAccessToken(),
                tokenResponse.getRefreshToken(),
                tokenResponse.getAccessExpiresIn(),
                tokenResponse.getRefreshExpiresIn(),
                roles(accessJwt)
        );
    }

    private UserCredentials createUserCredentials(TestInfo testInfo) {
        RegisterUserRequest registerUserRequest = createRegisterUserRequest(testInfo);
        sendRegisterUserRequest(registerUserRequest);

        return new UserCredentials(
                registerUserRequest.getUsername(),
                registerUserRequest.getPassword()
        );
    }

    private RegisterUserRequest createRegisterUserRequest(TestInfo testInfo) {
        String base = getPrefix(testInfo);

        return new RegisterUserRequest()
                .firstName("Test")
                .lastName("User")
                .username(base + "-username")
                .password(base + "-password")
                .city("Moscow");
    }

    private String getPrefix(TestInfo testInfo) {
        String methodName = testInfo.getTestMethod()
                .orElseThrow()
                .getName()
                .toLowerCase();

        return (methodName.length() > 20 ? methodName.substring(0, 20) : methodName) + "-" + java.util.UUID.randomUUID().toString().substring(0, 8);
    }

    private List<Role> roles(Jwt jwt) {
        return jwt.getClaimAsStringList("roles").stream()
                .map(Role::valueOf)
                .toList();
    }
}
