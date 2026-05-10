package com.elypsoeed.martlett.common.testdata;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "test")
public class TestDataProperties {

	private Api api = new Api();
	private Auth auth = new Auth();

	@Getter
	@Setter
	public static class Api {

		private String baseUrl;
	}

	@Getter
	@Setter
	public static class Auth {

		private String adminUsername;
		private String adminPassword;
	}
}
