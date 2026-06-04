package com.techstore.backend.product.application;

import java.util.List;

import com.techstore.backend.config.properties.LowStockMailProperties;
import com.techstore.backend.product.domain.Product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class AdminLowStockEmailSender {
	private static final Logger log = LoggerFactory.getLogger(AdminLowStockEmailSender.class);

	private final ObjectProvider<JavaMailSender> mailSenderProvider;
	private final LowStockMailProperties properties;

	public AdminLowStockEmailSender(ObjectProvider<JavaMailSender> mailSenderProvider, LowStockMailProperties properties) {
		this.mailSenderProvider = mailSenderProvider;
		this.properties = properties;
	}

	public boolean sendDailyAlert(List<Product> lowStockProducts, List<Product> outOfStockProducts) {
		if (lowStockProducts.isEmpty() && outOfStockProducts.isEmpty()) {
			return false;
		}

		JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
		if (!properties.enabled() || properties.recipient() == null || mailSender == null) {
			log.info("Alerta de stock bajo omitida: correo no configurado");
			return false;
		}

		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(properties.recipient());
			message.setFrom(properties.from());
			message.setSubject(properties.subject());
			message.setText(buildBody(lowStockProducts, outOfStockProducts));
			mailSender.send(message);
			return true;
		} catch (MailException exception) {
			log.error("No se pudo enviar alerta administrativa de stock bajo", exception);
			return false;
		}
	}

	private String buildBody(List<Product> lowStockProducts, List<Product> outOfStockProducts) {
		return """
				Hola,

				Estos productos necesitan reposicion en TechStore Pro.

				Stock bajo
				%s

				Agotados
				%s
				""".formatted(productLines(lowStockProducts), productLines(outOfStockProducts));
	}

	private String productLines(List<Product> products) {
		if (products.isEmpty()) {
			return "- Sin productos en esta seccion.";
		}

		StringBuilder lines = new StringBuilder("ID | Producto | Categoria | Stock actual");
		for (Product product : products) {
			lines.append(System.lineSeparator())
					.append(product.getId() == null ? "sin-id" : product.getId())
					.append(" | ")
					.append(product.getName())
					.append(" | ")
					.append(product.getCategory() == null ? "Sin categoria" : product.getCategory())
					.append(" | ")
					.append(product.getStock());
		}
		return lines.toString();
	}
}
