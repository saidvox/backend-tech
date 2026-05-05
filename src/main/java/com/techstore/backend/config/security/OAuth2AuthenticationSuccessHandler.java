package com.techstore.backend.config.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.techstore.backend.auth.application.OAuth2UserProvisioningService;
import com.techstore.backend.config.properties.OAuth2Properties;
import com.techstore.backend.user.domain.AppUser;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {
	private final OAuth2UserProvisioningService provisioningService;
	private final JwtService jwtService;
	private final OAuth2Properties oauth2Properties;

	public OAuth2AuthenticationSuccessHandler(
			OAuth2UserProvisioningService provisioningService,
			JwtService jwtService,
			OAuth2Properties oauth2Properties) {
		this.provisioningService = provisioningService;
		this.jwtService = jwtService;
		this.oauth2Properties = oauth2Properties;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
		AppUser user;
		try {
			user = isRegisterMode(request)
					? provisioningService.findOrCreateGoogleUserForRegistration(oauth2User)
					: provisioningService.findRegisteredGoogleUser(oauth2User);
		} catch (RuntimeException exception) {
			invalidateOAuth2Session(request);
			response.sendRedirect(failureUri(exception.getMessage()));
			return;
		}
		String token = jwtService.generateToken(user);
		invalidateOAuth2Session(request);
		response.sendRedirect(successUri(token));
	}

	private void invalidateOAuth2Session(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
	}

	private String successUri(String token) {
		String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
		return oauth2Properties.successRedirectUri() + "?token=" + encodedToken + "&tokenType=Bearer";
	}

	private boolean isRegisterMode(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		return session != null
				&& OAuth2ModeCaptureFilter.REGISTER_MODE.equals(
						session.getAttribute(OAuth2ModeCaptureFilter.OAUTH2_MODE_ATTRIBUTE));
	}

	private String failureUri(String message) {
		String safeMessage = message == null || message.isBlank()
				? "No se pudo iniciar sesion con Google"
				: message;
		String encodedMessage = URLEncoder.encode(safeMessage, StandardCharsets.UTF_8);
		return oauth2Properties.failureRedirectUri() + "?error=" + encodedMessage;
	}
}
