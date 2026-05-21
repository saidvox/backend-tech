package com.techstore.backend.order.application;

import java.math.BigDecimal;

import com.techstore.backend.config.properties.PurchaseMailProperties;
import com.techstore.backend.order.domain.OrderItem;
import com.techstore.backend.order.domain.PurchaseOrder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class OrderPurchaseEmailSender {
	private static final Logger log = LoggerFactory.getLogger(OrderPurchaseEmailSender.class);

	private final ObjectProvider<JavaMailSender> mailSenderProvider;
	private final PurchaseMailProperties properties;

	public OrderPurchaseEmailSender(ObjectProvider<JavaMailSender> mailSenderProvider, PurchaseMailProperties properties) {
		this.mailSenderProvider = mailSenderProvider;
		this.properties = properties;
	}

	public void sendPurchaseConfirmation(PurchaseOrder order) {
		JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
		if (!properties.enabled() || mailSender == null) {
			log.info("Notificacion de compra omitida para pedido {}: correo no configurado", order.getId());
			return;
		}

		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(order.getUser().getEmail());
		message.setFrom(properties.from());
		message.setSubject(properties.subject());
		message.setText(buildBody(order));

		try {
			mailSender.send(message);
		} catch (MailException exception) {
			log.error("No se pudo enviar notificacion de compra para pedido {}", order.getId(), exception);
		}
	}

	private String buildBody(PurchaseOrder order) {
		StringBuilder items = new StringBuilder();
		for (OrderItem item : order.getItems()) {
			items.append("- ")
					.append(item.getProduct().getName())
					.append(" x")
					.append(item.getQuantity())
					.append(" | S/ ")
					.append(money(item.getSubtotal()))
					.append(System.lineSeparator());
		}

		return """
				Hola %s,

				Tu compra en TechStore Pro fue confirmada correctamente.

				Pedido: #%s
				Estado: %s
				Total: S/ %s

				Productos:
				%s
				Gracias por tu compra.
				""".formatted(
						order.getUser().getName(),
						order.getId() == null ? "pendiente" : order.getId(),
						order.getStatus(),
						money(order.getTotal()),
						items);
	}

	private String money(BigDecimal value) {
		return value == null ? "0.00" : value.setScale(2).toPlainString();
	}
}
