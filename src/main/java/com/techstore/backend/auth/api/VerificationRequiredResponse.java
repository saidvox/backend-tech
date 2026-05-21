package com.techstore.backend.auth.api;

public record VerificationRequiredResponse(String email, boolean verificationRequired, String message) {
	public static VerificationRequiredResponse forEmail(String email, boolean emailSent) {
		return new VerificationRequiredResponse(
				email,
				true,
				emailSent
						? "Te enviamos un codigo de verificacion a tu correo."
						: "Correo no configurado. Revisa el codigo de verificacion en los logs del backend.");
	}
}
