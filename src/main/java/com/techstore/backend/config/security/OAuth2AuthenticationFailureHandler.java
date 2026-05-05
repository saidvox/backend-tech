package com.techstore.backend.config.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.techstore.backend.config.properties.OAuth2Properties;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {
	private final OAuth2Properties oauth2Properties;

	public OAuth2AuthenticationFailureHandler(OAuth2Properties oauth2Properties) {
		this.oauth2Properties = oauth2Properties;
	}

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
			throws IOException, ServletException {
		String message = URLEncoder.encode("No se pudo iniciar sesion con Google", StandardCharsets.UTF_8);
		response.sendRedirect(oauth2Properties.failureRedirectUri() + "?error=" + message);
	}
}
