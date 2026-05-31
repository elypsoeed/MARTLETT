package com.elypsoeed.martlett.git.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "app.git.storage")
public class GitStorageProperties {

	private String rootPath;

	public void setRootPath(String rootPath) {
		if (!StringUtils.hasText(rootPath)) {
			throw new IllegalArgumentException("app.git.storage.root-path must not be blank");
		}
		this.rootPath = rootPath;
	}
}
