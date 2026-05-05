package com.techstore.backend.config.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.techstore.backend.common.exception.ResourceNotFoundException;
import com.techstore.backend.user.domain.AppUser;
import com.techstore.backend.user.infrastructure.UserRepository;

@Service
public class CurrentUserService {
	private final UserRepository userRepository;

	public CurrentUserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public AppUser getCurrentUser() {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"));
	}

	public AppUser getCurrentUserOrNull() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
			return null;
		}
		return userRepository.findByEmail(authentication.getName()).orElse(null);
	}
}
