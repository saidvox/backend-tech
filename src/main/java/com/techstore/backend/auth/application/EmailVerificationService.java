package com.techstore.backend.auth.application;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;

import com.techstore.backend.common.exception.ApiException;
import com.techstore.backend.config.properties.VerificationMailProperties;
import com.techstore.backend.user.domain.AppUser;
import com.techstore.backend.user.infrastructure.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailVerificationService {
	private static final SecureRandom RANDOM = new SecureRandom();

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final VerificationEmailSender emailSender;
	private final VerificationMailProperties properties;
	private final Clock clock;

	@Autowired
	public EmailVerificationService(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			VerificationEmailSender emailSender,
			VerificationMailProperties properties) {
		this(userRepository, passwordEncoder, emailSender, properties, Clock.systemUTC());
	}

	EmailVerificationService(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			VerificationEmailSender emailSender,
			VerificationMailProperties properties,
			Clock clock) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.emailSender = emailSender;
		this.properties = properties;
		this.clock = clock;
	}

	@Transactional
	public boolean sendVerificationCode(AppUser user) {
		if (user.isEmailVerified()) {
			return true;
		}
		String code = generateCode();
		Instant now = Instant.now(clock);
		user.setEmailVerificationCode(
				passwordEncoder.encode(code),
				now.plus(properties.codeTtl()),
				now);
		userRepository.save(user);
		return emailSender.sendCode(user.getEmail(), user.getName(), code);
	}

	@Transactional
	public VerificationDelivery resendCode(String email) {
		AppUser user = userRepository.findByEmail(normalize(email))
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
		if (user.isEmailVerified()) {
			throw new ApiException(HttpStatus.CONFLICT, "El correo ya fue verificado");
		}
		boolean emailSent = sendVerificationCode(user);
		return new VerificationDelivery(user, emailSent);
	}

	@Transactional
	public AppUser verifyCode(String email, String code) {
		AppUser user = userRepository.findByEmail(normalize(email))
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
		if (user.isEmailVerified()) {
			return user;
		}
		if (user.getEmailVerificationCodeHash() == null || user.getEmailVerificationExpiresAt() == null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Solicita un nuevo codigo de verificacion");
		}
		if (Instant.now(clock).isAfter(user.getEmailVerificationExpiresAt())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "El codigo de verificacion expiro");
		}
		if (!passwordEncoder.matches(code, user.getEmailVerificationCodeHash())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Codigo de verificacion invalido");
		}
		user.markEmailVerified();
		return userRepository.save(user);
	}

	private String generateCode() {
		return "%06d".formatted(RANDOM.nextInt(1_000_000));
	}

	private String normalize(String email) {
		return email.trim().toLowerCase();
	}
}
