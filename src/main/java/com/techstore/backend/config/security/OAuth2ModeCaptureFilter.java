package com.techstore.backend.config.security;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2ModeCaptureFilter extends OncePerRequestFilter {
	static final String OAUTH2_MODE_ATTRIBUTE = "techstore.oauth2.mode";
	static final String REGISTER_MODE = "register";
	static final String LOGIN_MODE = "login";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		if (isGoogleAuthorizationRequest(request)) {
			String mode = REGISTER_MODE.equals(request.getParameter("mode")) ? REGISTER_MODE : LOGIN_MODE;
			request.getSession(true).setAttribute(OAUTH2_MODE_ATTRIBUTE, mode);
		}

		filterChain.doFilter(request, response);
	}

	private boolean isGoogleAuthorizationRequest(HttpServletRequest request) {
		return "GET".equalsIgnoreCase(request.getMethod())
				&& "/oauth2/authorization/google".equals(request.getRequestURI());
	}
}
