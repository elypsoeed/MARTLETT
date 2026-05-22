package com.elypsoeed.martlett.git.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "app.git.storage")
public class GitStorageProperties {

	private String rootPath;
}
