package com.elypsoeed.martlett.user.controller;

import com.elypsoeed.martlett.IntegrationTest;
import com.elypsoeed.martlett.common.entity.UserEntity;
import com.elypsoeed.martlett.common.repository.UserRepository;
import com.elypsoeed.martlett.common.testdata.TestData;
import com.elypsoeed.martlett.common.testdata.TestDataProperties;
import com.elypsoeed.martlett.common.testdata.model.TestUser;
import com.elypsoeed.martlett.generated.model.UpdateUserProfileRequest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
@RequiredArgsConstructor
public class UpdateUserProfileTest {

    private final TestData testData;
    private final TestDataProperties testDataProperties;
    private final UserRepository userRepository;

    @Test
    void noAuth() {
        Response response = patch(validRequest());

        assertThat(response.statusCode()).isEqualTo(401);
    }

    @Test
    void success(TestInfo testInfo) {
        TestUser testUser = testData.createAuthedUser(testInfo);

        Response response = patchAuthed(testUser.accessToken(), validRequest());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("username")).isEqualTo(testUser.username());
        assertThat(response.jsonPath().getString("firstName")).isEqualTo("Updated");
        assertThat(response.jsonPath().getString("lastName")).isEqualTo("Profile");
        assertThat(response.jsonPath().getString("sex")).isEqualTo("OTHER");
        assertThat(response.jsonPath().getInt("age")).isEqualTo(31);
        assertThat(response.jsonPath().getString("city")).isEqualTo("Saint Petersburg");
        assertThat(response.jsonPath().getString("tgContact")).isEqualTo("@updated");
        assertThat(response.jsonPath().getString("emailContact")).isEqualTo("updated@sanechka.com");
        assertThat(response.jsonPath().getString("placeOfWork")).isEqualTo("Martlett");

        UserEntity user = userRepository.findByUsername(testUser.username()).orElseThrow();
        assertThat(user.getFirstName()).isEqualTo("Updated");
        assertThat(user.getLastName()).isEqualTo("Profile");
        assertThat(user.getAge()).isEqualTo(31);
        assertThat(user.getCity()).isEqualTo("Saint Petersburg");
        assertThat(user.getTgContact()).isEqualTo("@updated");
        assertThat(user.getEmailContact()).isEqualTo("updated@sanechka.com");
        assertThat(user.getPlaceOfWork()).isEqualTo("Martlett");
    }

    @Test
    void invalidRequest(TestInfo testInfo) {
        TestUser testUser = testData.createAuthedUser(testInfo);
        UpdateUserProfileRequest request = validRequest().age(-1);

        Response response = patchAuthed(testUser.accessToken(), request);

        assertThat(response.statusCode()).isEqualTo(400);
    }

    private UpdateUserProfileRequest validRequest() {
        return new UpdateUserProfileRequest()
                .firstName("Updated")
                .lastName("Profile")
                .sex(UpdateUserProfileRequest.SexEnum.OTHER)
                .age(31)
                .city("Saint Petersburg")
                .tgContact("@updated")
                .emailContact("updated@sanechka.com")
                .placeOfWork("Martlett");
    }

    private Response patch(Object body) {
        RequestSpecification request = request().body(body);
        return executePatch(request);
    }

    private Response patchAuthed(String accessToken, Object body) {
        RequestSpecification request = request().body(body);
        request.auth().oauth2(accessToken);
        return executePatch(request);
    }

    private Response executePatch(RequestSpecification request) {
        return request
                .when()
                .patch("/api/me")
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
