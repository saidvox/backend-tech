package com.techstore.backend.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mail.purchase")
public record PurchaseMailProperties(
		boolean enabled,
		String from,
		String subject,
		String storeRecipient
) {
	public PurchaseMailProperties {
		if (from == null || from.isBlank()) {
			from = "TechStore Pro <no-reply@techstore.local>";
		}
		if (subject == null || subject.isBlank()) {
			subject = "Confirmacion de compra - TechStore Pro";
		}
		if (storeRecipient != null && storeRecipient.isBlank()) {
			storeRecipient = null;
		}
	}
}
