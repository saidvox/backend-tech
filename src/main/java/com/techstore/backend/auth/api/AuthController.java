package com.techstore.backend.auth.api;

import com.techstore.backend.auth.application.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticacion", description = "Registro e inicio de sesion con JWT")
public class AuthController {
	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Registrar usuario", responses = {
			@ApiResponse(responseCode = "201", description = "Usuario registrado"),
			@ApiResponse(responseCode = "409", description = "Correo ya registrado")
	})
	public VerificationRequiredResponse register(@Valid @RequestBody RegisterRequest request) {
		return authService.register(request);
	}

	@PostMapping("/login")
	@Operation(summary = "Iniciar sesion", responses = {
			@ApiResponse(responseCode = "200", description = "Credenciales validas"),
			@ApiResponse(responseCode = "401", description = "Credenciales invalidas")
	})
	public AuthResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

	@PostMapping("/verify-email")
	@Operation(summary = "Verificar correo", responses = {
			@ApiResponse(responseCode = "200", description = "Correo verificado"),
			@ApiResponse(responseCode = "400", description = "Codigo invalido o expirado")
	})
	public AuthResponse verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
		return authService.verifyEmail(request);
	}

	@PostMapping("/resend-verification")
	@Operation(summary = "Reenviar codigo de verificacion", responses = {
			@ApiResponse(responseCode = "200", description = "Codigo reenviado"),
			@ApiResponse(responseCode = "404", description = "Usuario no encontrado")
	})
	public VerificationRequiredResponse resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
		return authService.resendVerification(request);
	}
}
