package com.techstore.backend.auth.application;

import com.techstore.backend.auth.api.AuthResponse;
import com.techstore.backend.auth.api.LoginRequest;
import com.techstore.backend.auth.api.RegisterRequest;
import com.techstore.backend.common.exception.ApiException;
import com.techstore.backend.config.security.JwtService;
import com.techstore.backend.user.api.UserResponse;
import com.techstore.backend.user.domain.AppUser;
import com.techstore.backend.user.domain.Role;
import com.techstore.backend.user.infrastructure.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		String email = request.email().trim().toLowerCase();
		if (userRepository.existsByEmail(email)) {
			throw new ApiException(HttpStatus.CONFLICT, "El correo ya esta registrado");
		}
		AppUser user = userRepository.save(new AppUser(
				request.name().trim(),
				email,
				passwordEncoder.encode(request.password()),
				Role.USER));
		return response(user);
	}

	public AuthResponse login(LoginRequest request) {
		AppUser user = userRepository.findByEmail(request.email().trim().toLowerCase())
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas"));
		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas");
		}
		return response(user);
	}

	private AuthResponse response(AppUser user) {
		return new AuthResponse(jwtService.generateToken(user), "Bearer", UserResponse.from(user));
	}
}
