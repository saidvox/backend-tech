package com.techstore.backend.auth.application;

import com.techstore.backend.config.properties.VerificationMailProperties;
import com.techstore.backend.common.exception.ApiException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class VerificationEmailSender {
	private static final Logger log = LoggerFactory.getLogger(VerificationEmailSender.class);

	private final ObjectProvider<JavaMailSender> mailSenderProvider;
	private final VerificationMailProperties properties;

	public VerificationEmailSender(ObjectProvider<JavaMailSender> mailSenderProvider, VerificationMailProperties properties) {
		this.mailSenderProvider = mailSenderProvider;
		this.properties = properties;
	}

	public boolean sendCode(String to, String name, String code) {
		JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
		if (!properties.enabled() || mailSender == null) {
			log.warn("Codigo de verificacion para {}: {}", to, code);
			return false;
		}

		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(to);
		message.setFrom(properties.from());
		message.setSubject(properties.subject());
		message.setText("""
				Hola %s,

				Tu codigo de verificacion de TechStore Pro es: %s

				Este codigo vence en %d minutos.
				Si no solicitaste este registro, puedes ignorar este mensaje.
				""".formatted(
						StringUtils.hasText(name) ? name : "usuario",
						code,
						properties.codeTtl().toMinutes()));

		try {
			mailSender.send(message);
			return true;
		} catch (MailAuthenticationException exception) {
			log.error("Gmail rechazo las credenciales SMTP configuradas para enviar a {}", to, exception);
			throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
					"Gmail rechazo las credenciales SMTP. Verifica que MAIL_PASSWORD sea una contrasena de aplicacion de 16 caracteres.");
		} catch (MailException exception) {
			log.error("No se pudo enviar codigo de verificacion a {}", to, exception);
			throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
					"No se pudo enviar el correo de verificacion. Revisa la configuracion SMTP.");
		}
	}
}
