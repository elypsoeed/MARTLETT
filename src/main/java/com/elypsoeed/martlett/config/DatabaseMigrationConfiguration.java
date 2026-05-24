package com.elypsoeed.martlett.config;

import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.Arrays;

@Configuration
public class DatabaseMigrationConfiguration {

	@Bean
	Flyway flyway(DataSource dataSource, Environment environment) {
		String locations = environment.getProperty("spring.flyway.locations", "classpath:db/migration");
		boolean cleanOnStart = environment.getProperty("app.flyway.rerun-migrations", Boolean.class, false);
		Flyway flyway = Flyway.configure()
			.dataSource(dataSource)
			.locations(Arrays.stream(locations.split(","))
				.map(String::trim)
				.filter(location -> !location.isEmpty())
				.toArray(String[]::new))
			.load();
		if (cleanOnStart) {
			flyway.clean();
		}
		flyway.migrate();
		return flyway;
	}
}
