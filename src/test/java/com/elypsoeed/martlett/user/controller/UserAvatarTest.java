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

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
@RequiredArgsConstructor
public class UserAvatarTest {

	private final TestData testData;
	private final TestDataProperties testDataProperties;

	@Test
	void noAuth() throws IOException {
		Response response = updateAvatar(null, avatarBytes(), "image/png");

		assertThat(response.statusCode()).isEqualTo(401);
	}

	@Test
	void success(TestInfo testInfo) throws IOException {
		TestUser testUser = testData.createAuthedUser(testInfo);
		byte[] originalAvatar = avatarBytes();

		Response updateResponse = updateAvatar(testUser.accessToken(), originalAvatar, "image/png");

		assertThat(updateResponse.statusCode()).isEqualTo(200);
		assertThat(updateResponse.jsonPath().getString("avatarUrl"))
			.isEqualTo("/api/users/" + testUser.username() + "/avatar");

		Response getProfileResponse = getProfile(testUser.accessToken());
		assertThat(getProfileResponse.statusCode()).isEqualTo(200);
		assertThat(getProfileResponse.jsonPath().getString("avatarUrl"))
			.isEqualTo("/api/users/" + testUser.username() + "/avatar");

		Response getAvatarResponse = getAvatar(testUser.username());
		assertThat(getAvatarResponse.statusCode()).isEqualTo(200);
		assertThat(getAvatarResponse.contentType()).startsWith("image/jpeg");

		byte[] normalizedAvatar = getAvatarResponse.getBody().asByteArray();
		assertThat(normalizedAvatar).isNotEqualTo(originalAvatar);

		BufferedImage image = ImageIO.read(new ByteArrayInputStream(normalizedAvatar));
		assertThat(image.getWidth()).isEqualTo(256);
		assertThat(image.getHeight()).isEqualTo(256);
	}

	@Test
	void plainText(TestInfo testInfo) {
		TestUser testUser = testData.createAuthedUser(testInfo);

		Response response = updateAvatar(testUser.accessToken(), "plain text".getBytes(), "text/plain");

		assertThat(response.statusCode()).isEqualTo(400);
	}

	@Test
	void invalidImage(TestInfo testInfo) {
		TestUser testUser = testData.createAuthedUser(testInfo);

		Response response = updateAvatar(testUser.accessToken(), "not an image".getBytes(), "image/png");

		assertThat(response.statusCode()).isEqualTo(400);
	}

	@Test
	void missingAvatar(TestInfo testInfo) {
		TestUser testUser = testData.createAuthedUser(testInfo);

		Response response = getAvatar(testUser.username());

		assertThat(response.statusCode()).isEqualTo(404);
	}

	private Response updateAvatar(String accessToken, byte[] fileBytes, String contentType) {
		RequestSpecification request = request()
			.accept("application/json")
			.multiPart("file", "avatar.png", fileBytes, contentType);

		if (accessToken != null) {
			request.auth().oauth2(accessToken);
		}

		return request
			.when()
			.put("/api/me/avatar")
			.then()
			.extract()
			.response();
	}

	private Response getProfile(String accessToken) {
		return request()
			.auth().oauth2(accessToken)
			.accept("application/json")
			.when()
			.get("/api/me")
			.then()
			.extract()
			.response();
	}

	private Response getAvatar(String username) {
		return request()
			.when()
			.get("/api/users/" + username + "/avatar")
			.then()
			.extract()
			.response();
	}

	private RequestSpecification request() {
		return given()
			.baseUri(testDataProperties.getApiBaseUrl())
			.port(testDataProperties.testPort());
	}

	private byte[] avatarBytes() throws IOException {
		BufferedImage image = new BufferedImage(512, 320, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		try {
			graphics.setColor(Color.RED);
			graphics.fillRect(0, 0, 256, 320);
			graphics.setColor(Color.BLUE);
			graphics.fillRect(256, 0, 256, 320);
		} finally {
			graphics.dispose();
		}

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ImageIO.write(image, "png", output);
		return output.toByteArray();
	}
}
