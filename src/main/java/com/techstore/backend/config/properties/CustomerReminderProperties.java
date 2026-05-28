package com.techstore.backend.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.reminders.customer")
public record CustomerReminderProperties(
		boolean enabled,
		String cron,
		String subject,
		int maxRelated,
		String frontendProductUrl
) {
	public CustomerReminderProperties {
		if (cron == null || cron.isBlank()) {
			cron = "0 0 9 * * *";
		}
		if (subject == null || subject.isBlank()) {
			subject = "Ofertas para tus favoritos - TechStore Pro";
		}
		if (maxRelated < 1) {
			maxRelated = 3;
		}
		if (frontendProductUrl == null || frontendProductUrl.isBlank()) {
			frontendProductUrl = "http://localhost:4200/catalogo";
		}
	}
}
