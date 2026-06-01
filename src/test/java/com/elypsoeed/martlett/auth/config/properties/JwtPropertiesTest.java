package com.elypsoeed.martlett.auth.config.properties;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtPropertiesTest {

	@Test
	void success() {
		assertThatCode(() -> new JwtProperties(
			"0123456789abcdef0123456789abcdef",
			3600,
			1209600,
			"martlett"
		)).doesNotThrowAnyException();
	}

	@Test
	void rejectsShortSecret() {
		assertThatThrownBy(() -> new JwtProperties("short", 3600, 1209600, "martlett"))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("app.jwt.secret must be at least 256 bits");
	}
}
