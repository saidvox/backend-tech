package com.techstore.backend.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "app.storage.google-drive")
public record GoogleDriveStorageProperties(
		boolean enabled,
		String folderId,
		String credentialsPath,
		String credentialsBase64,
		String clientId,
		String clientSecret,
		String refreshToken,
		long maxImageSizeBytes
) {
	public boolean hasCredentials() {
		return hasServiceAccountCredentials() || hasOAuthCredentials();
	}

	public boolean hasServiceAccountCredentials() {
		return StringUtils.hasText(credentialsPath) || StringUtils.hasText(credentialsBase64);
	}

	public boolean hasOAuthCredentials() {
		return StringUtils.hasText(clientId)
				&& StringUtils.hasText(clientSecret)
				&& StringUtils.hasText(refreshToken);
	}
}
