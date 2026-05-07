package com.techstore.backend.config;

import javax.sql.DataSource;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DatabaseSchemaCompatibilityConfig {
	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	CommandLineRunner alignPostgresEnumConstraints(DataSource dataSource, JdbcTemplate jdbcTemplate) {
		return args -> {
			try (var connection = dataSource.getConnection()) {
				String database = connection.getMetaData().getDatabaseProductName();
				if (!database.toLowerCase().contains("postgresql")) {
					return;
				}
			}

			jdbcTemplate.execute("ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_status_check");
			jdbcTemplate.execute("""
					ALTER TABLE orders
					ADD CONSTRAINT orders_status_check
					CHECK (status IN ('PENDING_PAYMENT', 'CONFIRMED', 'CANCELLED', 'DELIVERED'))
					""");
			jdbcTemplate.execute("ALTER TABLE payments DROP CONSTRAINT IF EXISTS payments_provider_check");
			jdbcTemplate.execute("""
					ALTER TABLE payments
					ADD CONSTRAINT payments_provider_check
					CHECK (provider IN ('MERCADO_PAGO_SIMULATED', 'MERCADO_PAGO'))
					""");
		};
	}
}
