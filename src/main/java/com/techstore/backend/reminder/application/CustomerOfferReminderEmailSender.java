package com.techstore.backend.reminder.application;

import java.math.BigDecimal;
import java.util.List;

import com.techstore.backend.config.properties.CustomerReminderProperties;
import com.techstore.backend.config.properties.PurchaseMailProperties;
import com.techstore.backend.product.domain.Product;
import com.techstore.backend.user.domain.AppUser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class CustomerOfferReminderEmailSender {
	private static final Logger log = LoggerFactory.getLogger(CustomerOfferReminderEmailSender.class);

	private final ObjectProvider<JavaMailSender> mailSenderProvider;
	private final PurchaseMailProperties mailProperties;
	private final CustomerReminderProperties reminderProperties;

	public CustomerOfferReminderEmailSender(
			ObjectProvider<JavaMailSender> mailSenderProvider,
			PurchaseMailProperties mailProperties,
			CustomerReminderProperties reminderProperties) {
		this.mailSenderProvider = mailSenderProvider;
		this.mailProperties = mailProperties;
		this.reminderProperties = reminderProperties;
	}

	public boolean send(AppUser user, List<Product> favoriteOffers, List<Product> relatedOffers) {
		JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
		if (!reminderProperties.enabled() || !mailProperties.enabled() || mailSender == null) {
			log.info("Recordatorio de ofertas omitido para {}: correo no configurado", user.getEmail());
			return false;
		}
		if (favoriteOffers.isEmpty() && relatedOffers.isEmpty()) {
			return false;
		}

		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(user.getEmail());
			message.setFrom(mailProperties.from());
			message.setSubject(reminderProperties.subject());
			message.setText(buildBody(user, favoriteOffers, relatedOffers));
			mailSender.send(message);
			return true;
		} catch (MailException exception) {
			log.error("No se pudo enviar recordatorio de ofertas a {}", user.getEmail(), exception);
			return false;
		}
	}

	private String buildBody(AppUser user, List<Product> favoriteOffers, List<Product> relatedOffers) {
		return """
				Hola %s,

				Encontramos ofertas que pueden interesarte en TechStore Pro.

				Favoritos en oferta:
				%s

				Relacionados en oferta:
				%s

				Revisa el catalogo: %s
				""".formatted(
						user.getName(),
						productLines(favoriteOffers),
						productLines(relatedOffers),
						reminderProperties.frontendProductUrl());
	}

	private String productLines(List<Product> products) {
		if (products.isEmpty()) {
			return "- Sin novedades por ahora.";
		}
		return products.stream()
				.map(product -> "- %s | Antes S/ %s | Oferta S/ %s | %s".formatted(
						product.getName(),
						money(product.getPrice()),
						money(product.getEffectivePrice()),
						reminderProperties.frontendProductUrl()))
				.toList()
				.stream()
				.reduce((left, right) -> left + System.lineSeparator() + right)
				.orElse("");
	}

	private String money(BigDecimal value) {
		return value == null ? "0.00" : value.setScale(2).toPlainString();
	}
}
