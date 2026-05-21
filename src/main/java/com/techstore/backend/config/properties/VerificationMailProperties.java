package com.techstore.backend.config.properties;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mail.verification")
public record VerificationMailProperties(
		boolean enabled,
		String from,
		Duration codeTtl,
		String subject
) {
	public VerificationMailProperties {
		if (from == null || from.isBlank()) {
			from = "TechStore Pro <no-reply@techstore.local>";
		}
		if (codeTtl == null) {
			codeTtl = Duration.ofMinutes(15);
		}
		if (subject == null || subject.isBlank()) {
			subject = "Codigo de verificacion - TechStore Pro";
		}
	}
}
