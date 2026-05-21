package com.techstore.backend.auth.application;

import com.techstore.backend.auth.api.AuthResponse;
import com.techstore.backend.auth.api.LoginRequest;
import com.techstore.backend.auth.api.RegisterRequest;
import com.techstore.backend.auth.api.ResendVerificationRequest;
import com.techstore.backend.auth.api.VerificationRequiredResponse;
import com.techstore.backend.auth.api.VerifyEmailRequest;
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
	private final EmailVerificationService emailVerificationService;

	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, EmailVerificationService emailVerificationService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.emailVerificationService = emailVerificationService;
	}

	@Transactional
	public VerificationRequiredResponse register(RegisterRequest request) {
		String email = request.email().trim().toLowerCase();
		if (userRepository.existsByEmail(email)) {
			throw new ApiException(HttpStatus.CONFLICT, "El correo ya esta registrado");
		}
		AppUser user = new AppUser(
				request.name().trim(),
				email,
				passwordEncoder.encode(request.password()),
				Role.USER);
		user.markEmailUnverified();
		user = userRepository.save(user);
		boolean emailSent = emailVerificationService.sendVerificationCode(user);
		return VerificationRequiredResponse.forEmail(user.getEmail(), emailSent);
	}

	public AuthResponse login(LoginRequest request) {
		AppUser user = userRepository.findByEmail(request.email().trim().toLowerCase())
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas"));
		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas");
		}
		if (!user.isEmailVerified()) {
			throw new ApiException(HttpStatus.FORBIDDEN, "El correo no esta verificado");
		}
		return response(user);
	}

	public AuthResponse verifyEmail(VerifyEmailRequest request) {
		AppUser user = emailVerificationService.verifyCode(request.email().trim().toLowerCase(), request.code());
		return response(user);
	}

	public VerificationRequiredResponse resendVerification(ResendVerificationRequest request) {
		VerificationDelivery delivery = emailVerificationService.resendCode(request.email().trim().toLowerCase());
		return VerificationRequiredResponse.forEmail(delivery.user().getEmail(), delivery.emailSent());
	}

	private AuthResponse response(AppUser user) {
		return new AuthResponse(
				jwtService.generateToken(user),
				"Bearer",
				UserResponse.from(user),
				user.getEmail(),
				false,
				null);
	}
}
