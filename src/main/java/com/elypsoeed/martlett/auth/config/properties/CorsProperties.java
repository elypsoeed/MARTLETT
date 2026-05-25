package com.elypsoeed.martlett.auth.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

	private List<String> allowedOrigins = new ArrayList<>();

	public List<String> getAllowedOrigins() {
		return allowedOrigins;
	}
}
