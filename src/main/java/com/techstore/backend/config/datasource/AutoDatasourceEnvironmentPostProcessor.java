package com.techstore.backend.config.datasource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

public class AutoDatasourceEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {
	private static final String PROPERTY_SOURCE_NAME = "autoDetectedDatasource";

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		if (!environment.getProperty("app.datasource.auto-detect", Boolean.class, true)) {
			return;
		}
		if (hasExplicitProfile(environment) || hasExplicitDatasourceUrl(environment)) {
			return;
		}

		String url = environment.getProperty("DB_URL", "jdbc:postgresql://localhost:5432/techstore");
		String username = environment.getProperty("DB_USERNAME", "techstore");
		String password = environment.getProperty("DB_PASSWORD", "techstore123");

		if (!canConnect(url, username, password)) {
			return;
		}

		Map<String, Object> properties = new HashMap<>();
		properties.put("spring.datasource.url", url);
		properties.put("spring.datasource.driver-class-name", "org.postgresql.Driver");
		properties.put("spring.datasource.username", username);
		properties.put("spring.datasource.password", password);
		properties.put("spring.jpa.hibernate.ddl-auto", "update");
		properties.put("spring.h2.console.enabled", "false");
		environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, properties));
	}

	private boolean hasExplicitProfile(ConfigurableEnvironment environment) {
		return environment.getActiveProfiles().length > 0
				|| StringUtils.hasText(environment.getProperty("spring.profiles.active"));
	}

	private boolean hasExplicitDatasourceUrl(ConfigurableEnvironment environment) {
		return StringUtils.hasText(environment.getProperty("spring.datasource.url"))
				|| StringUtils.hasText(environment.getProperty("SPRING_DATASOURCE_URL"));
	}

	private boolean canConnect(String url, String username, String password) {
		int previousTimeout = DriverManager.getLoginTimeout();
		DriverManager.setLoginTimeout(2);
		try (Connection ignored = DriverManager.getConnection(url, username, password)) {
			return true;
		} catch (SQLException exception) {
			return false;
		} finally {
			DriverManager.setLoginTimeout(previousTimeout);
		}
	}

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}
}
