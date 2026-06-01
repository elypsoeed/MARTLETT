package com.elypsoeed.martlett.git.config.properties;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GitStoragePropertiesTest {

	@Test
	void success() {
		GitStorageProperties properties = new GitStorageProperties();

		properties.setRootPath("/var/lib/martlett/git");

		assertThat(properties.getRootPath()).isEqualTo("/var/lib/martlett/git");
	}

	@Test
	void blankRootPath() {
		GitStorageProperties properties = new GitStorageProperties();

		assertThatThrownBy(() -> properties.setRootPath(" "))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("app.git.storage.root-path must not be blank");
	}
}
