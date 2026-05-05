package com.techstore.backend.auth.application;

import com.techstore.backend.common.exception.BadRequestException;
import com.techstore.backend.user.domain.AppUser;
import com.techstore.backend.user.domain.Role;
import com.techstore.backend.user.infrastructure.UserRepository;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OAuth2UserProvisioningService {
	private static final String GOOGLE_PROVIDER = "google";

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public OAuth2UserProvisioningService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public AppUser findRegisteredGoogleUser(OAuth2User oauth2User) {
		String email = requiredAttribute(oauth2User, "email").trim().toLowerCase();
		String providerId = requiredAttribute(oauth2User, "sub");
		String name = attributeOrDefault(oauth2User, "name", email);

		return userRepository.findByEmail(email)
				.map(user -> linkGoogleAccount(user, providerId, name))
				.orElseThrow(() -> new BadRequestException(
						"Primero debes registrarte con tu email antes de iniciar sesion con Google"));
	}

	@Transactional
	public AppUser findOrCreateGoogleUserForRegistration(OAuth2User oauth2User) {
		String email = requiredAttribute(oauth2User, "email").trim().toLowerCase();
		String providerId = requiredAttribute(oauth2User, "sub");
		String name = attributeOrDefault(oauth2User, "name", email);

		return userRepository.findByEmail(email)
				.map(user -> linkGoogleAccount(user, providerId, name))
				.orElseGet(() -> createGoogleUser(name, email, providerId));
	}

	private AppUser createGoogleUser(String name, String email, String providerId) {
		String generatedPassword = passwordEncoder.encode(UUID.randomUUID().toString());
		AppUser user = new AppUser(name, email, generatedPassword, Role.USER);
		user.linkOAuth2Account(GOOGLE_PROVIDER, providerId, name);
		return userRepository.save(user);
	}

	private AppUser linkGoogleAccount(AppUser user, String providerId, String name) {
		user.linkOAuth2Account(GOOGLE_PROVIDER, providerId, name);
		return user;
	}

	private String requiredAttribute(OAuth2User oauth2User, String name) {
		Object value = oauth2User.getAttribute(name);
		if (value == null || value.toString().isBlank()) {
			throw new IllegalArgumentException("Google no devolvio el atributo requerido: " + name);
		}
		return value.toString();
	}

	private String attributeOrDefault(OAuth2User oauth2User, String name, String defaultValue) {
		Object value = oauth2User.getAttribute(name);
		return value == null || value.toString().isBlank() ? defaultValue : value.toString();
	}
}
