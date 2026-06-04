package com.techstore.backend.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mail.low-stock")
public record LowStockMailProperties(
		boolean enabled,
		String from,
		String recipient,
		String subject,
		String cron,
		int threshold,
		boolean includeOutOfStock
) {
	public LowStockMailProperties {
		if (from == null || from.isBlank()) {
			from = "TechStore Pro <no-reply@techstore.local>";
		}
		if (recipient != null && recipient.isBlank()) {
			recipient = null;
		}
		if (subject == null || subject.isBlank()) {
			subject = "Alerta de inventario - TechStore Pro";
		}
		if (cron == null || cron.isBlank()) {
			cron = "0 0 8 * * *";
		}
		if (threshold < 1) {
			threshold = 5;
		}
	}
}
