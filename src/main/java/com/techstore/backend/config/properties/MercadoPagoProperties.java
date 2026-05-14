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
		String pendingUrl,
		String testPayerEmail,
		boolean walletPurchaseOnly
) {
	public boolean realMode() {
		return "real".equalsIgnoreCase(mode)
				|| "production".equalsIgnoreCase(mode)
				|| "sandbox".equalsIgnoreCase(mode)
				|| "test".equalsIgnoreCase(mode);
	}

	public boolean sandboxMode() {
		return "sandbox".equalsIgnoreCase(mode)
				|| "test".equalsIgnoreCase(mode);
	}

	public boolean productionMode() {
		return "real".equalsIgnoreCase(mode)
				|| "production".equalsIgnoreCase(mode);
	}

	public boolean hasAccessToken() {
		return StringUtils.hasText(accessToken);
	}

	public boolean hasTestPayerEmail() {
		return StringUtils.hasText(testPayerEmail);
	}
}
