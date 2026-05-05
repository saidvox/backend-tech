package com.techstore.backend.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.oauth2")
public record OAuth2Properties(
		String successRedirectUri,
		String failureRedirectUri
) {
}
