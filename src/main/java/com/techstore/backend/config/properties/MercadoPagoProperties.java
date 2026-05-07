package com.techstore.backend.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "app.payments.mercado-pago")
public record MercadoPagoProperties(
		String mode,
		String accessToken,
		String apiBaseUrl,
		String simulatedCheckoutUrl,
		String notificationUrl,
		String successUrl,
		String failureUrl,
		String pendingUrl
) {
	public boolean realMode() {
		return "real".equalsIgnoreCase(mode);
	}

	public boolean hasAccessToken() {
		return StringUtils.hasText(accessToken);
	}
}
