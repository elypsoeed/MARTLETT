package com.elypsoeed.martlett.common.testdata;

import com.elypsoeed.martlett.auth.config.JwtDecoderFactory;
import com.elypsoeed.martlett.auth.model.Role;
import com.elypsoeed.martlett.generated.model.RefreshTokenRequest;
import com.elypsoeed.martlett.generated.model.RegisterUserRequest;
import com.elypsoeed.martlett.generated.model.TokenRequest;
import com.fasterxml.jackson.databind.JsonNode;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@Component
@RequiredArgsConstructor
public class TestData {

    private final Environment environment;
    private final JwtDecoderFactory jwtDecoderFactory;
    private final TestDataProperties testDataProperties;
    private final ConcurrentMap<Class<?>, RegisteredUserCredentials> registeredUsersByClass = new ConcurrentHashMap<>();

    public TestUser getAuthedUser(Class<?> testClass) {
        RegisteredUserCredentials credentials = registeredUsersByClass.computeIfAbsent(
                testClass,
                this::registerUserForClass
        );
        Response response = issueTokenResponse(credentials.username(), credentials.password());
        assertThat(response.statusCode()).isEqualTo(200);
        return toLoggedInUser(credentials.username(), response.as(JsonNode.class));
    }

    public TestUser getAuthedAdmin() {
        Response response = issueTokenResponse(
                testDataProperties.getAuth().getAdminUsername(),
                testDataProperties.getAuth().getAdminPassword()
        );
        assertThat(response.statusCode()).isEqualTo(200);
        return toLoggedInUser(testDataProperties.getAuth().getAdminUsername(), response.as(JsonNode.class));
    }

    public TestUser refreshToken(TestUser testUser) {
        Response response = refreshTokenResponse(testUser.refreshToken());
        assertThat(response.statusCode()).isEqualTo(200);
        return toLoggedInUser(testUser.username(), response.as(JsonNode.class));
    }

    public Response registerUserResponse(RegisterUserRequest registerUserRequest) {
        return given()
                .baseUri(testDataProperties.getApi().getBaseUrl())
                .port(port())
                .contentType("application/json")
                .accept("application/json")
                .body(registerUserRequest)
                .when()
                .post("/api/auth/register")
                .then()
                .extract()
                .response();
    }

    public Response issueTokenResponse(String username, String password) {
        return given()
                .baseUri(testDataProperties.getApi().getBaseUrl())
                .port(port())
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

    public Response refreshTokenResponse(String refreshToken) {
        return given()
                .baseUri(testDataProperties.getApi().getBaseUrl())
                .port(port())
                .contentType("application/json")
                .accept("application/json")
                .body(new RefreshTokenRequest()
                        .refreshToken(refreshToken))
                .when()
                .post("/api/auth/refresh")
                .then()
                .extract()
                .response();
    }

    public RegisterUserRequest newRegistrationRequest(Class<?> testClass, String suffix) {
        String normalizedSuffix = suffix.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        String base = testClass.getSimpleName().toLowerCase() + "-" + normalizedSuffix;

        return new RegisterUserRequest()
                .name("Test")
                .surname("User")
                .nickname(base + "-nick")
                .username(base + "-login")
                .password(base + "-password")
                .city("Moscow");
    }

    private TestUser toLoggedInUser(String username, JsonNode json) {
        assertThat(json.path("tokenType").asText()).isEqualTo("Bearer");
        assertThat(json.path("accessExpiresIn").asLong()).isEqualTo(3600);
        assertThat(json.path("refreshExpiresIn").asLong()).isEqualTo(1209600);
        assertThat(json.path("accessToken").asText()).isNotBlank();
        assertThat(json.path("refreshToken").asText()).isNotBlank();

        Jwt accessJwt = jwtDecoderFactory.createAccessTokenDecoder().decode(json.path("accessToken").asText());

        return new TestUser(
                username,
                json.path("accessToken").asText(),
                json.path("refreshToken").asText(),
                json.path("accessExpiresIn").asLong(),
                json.path("refreshExpiresIn").asLong(),
                roles(accessJwt)
        );
    }

    private RegisteredUserCredentials registerUserForClass(Class<?> testClass) {
        RegisterUserRequest registerUserRequest = newRegistrationRequest(testClass, "shared");
        Response response = registerUserResponse(registerUserRequest);

        assertThat(response.statusCode()).isEqualTo(201);

        return new RegisteredUserCredentials(registerUserRequest.getUsername(), registerUserRequest.getPassword());
    }

    private int port() {
        return Integer.parseInt(environment.getRequiredProperty("local.server.port"));
    }

    public record TestUser(
            String username,
            String accessToken,
            String refreshToken,
            long accessExpiresIn,
            long refreshExpiresIn,
            List<Role> roles
    ) {
    }

    private List<Role> roles(Jwt jwt) {
        return jwt.getClaimAsStringList("roles").stream()
                .map(Role::valueOf)
                .toList();
    }

    private record RegisteredUserCredentials(
            String username,
            String password
    ) {
    }
}
