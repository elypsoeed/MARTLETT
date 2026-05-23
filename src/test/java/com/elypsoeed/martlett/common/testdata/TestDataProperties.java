package com.elypsoeed.martlett.common.testdata;

import lombok.RequiredArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Getter
@Setter
@ConfigurationProperties(prefix = "test")
public class TestDataProperties {

	private final Environment environment;
	private String apiBaseUrl;
	private String adminUsername;
	private String adminPassword;

	public int testPort() {
		return Integer.parseInt(environment.getRequiredProperty("local.server.port"));
	}
}
