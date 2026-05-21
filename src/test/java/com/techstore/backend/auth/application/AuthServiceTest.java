package com.techstore.backend.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.techstore.backend.auth.api.LoginRequest;
import com.techstore.backend.auth.api.RegisterRequest;
import com.techstore.backend.auth.api.VerifyEmailRequest;
import com.techstore.backend.common.exception.ApiException;
import com.techstore.backend.config.security.JwtService;
import com.techstore.backend.user.domain.AppUser;
import com.techstore.backend.user.domain.Role;
import com.techstore.backend.user.infrastructure.UserRepository;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtService jwtService;

	@Mock
	private EmailVerificationService emailVerificationService;

	@Captor
	private ArgumentCaptor<AppUser> userCaptor;

	@Test
	void registerCreatesUnverifiedUserAndStartsEmailVerification() {
		AuthService authService = authService();
		when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
		when(passwordEncoder.encode("secret123")).thenReturn("encoded");
		when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(emailVerificationService.sendVerificationCode(any(AppUser.class))).thenReturn(true);

		var response = authService.register(new RegisterRequest("New User", " new@example.com ", "secret123"));

		verify(userRepository).save(userCaptor.capture());
		AppUser savedUser = userCaptor.getValue();
		assertThat(savedUser.isEmailVerified()).isFalse();
		verify(emailVerificationService).sendVerificationCode(savedUser);
		assertThat(response.email()).isEqualTo("new@example.com");
		assertThat(response.verificationRequired()).isTrue();
		assertThat(response.message()).contains("correo");
		verify(jwtService, never()).generateToken(any());
	}

	@Test
	void loginRejectsUnverifiedUserWithoutIssuingToken() {
		AuthService authService = authService();
		AppUser user = new AppUser("New User", "new@example.com", "encoded", Role.USER);
		user.markEmailUnverified();
		when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("secret123", "encoded")).thenReturn(true);

		assertThatThrownBy(() -> authService.login(new LoginRequest("new@example.com", "secret123")))
				.isInstanceOf(ApiException.class)
				.extracting("status")
				.isEqualTo(HttpStatus.FORBIDDEN);

		verify(jwtService, never()).generateToken(any());
	}

	@Test
	void verifyEmailMarksUserAsVerifiedAndIssuesToken() {
		AuthService authService = authService();
		AppUser user = new AppUser("New User", "new@example.com", "encoded", Role.USER);
		user.markEmailVerified();
		when(emailVerificationService.verifyCode("new@example.com", "123456")).thenReturn(user);
		when(jwtService.generateToken(user)).thenReturn("jwt-token");

		var response = authService.verifyEmail(new VerifyEmailRequest("new@example.com", "123456"));

		assertThat(response.token()).isEqualTo("jwt-token");
		assertThat(response.user().email()).isEqualTo("new@example.com");
	}

	private AuthService authService() {
		return new AuthService(userRepository, passwordEncoder, jwtService, emailVerificationService);
	}
}
