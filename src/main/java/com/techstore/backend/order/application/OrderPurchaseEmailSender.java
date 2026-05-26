package com.techstore.backend.order.application;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

		sendCustomerConfirmation(mailSender, order);
		sendStoreNotification(mailSender, order);
	}

	private void sendCustomerConfirmation(JavaMailSender mailSender, PurchaseOrder order) {
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(order.getUser().getEmail());
			message.setFrom(properties.from());
			message.setSubject(properties.subject());
			message.setText(buildCustomerBody(order));
			mailSender.send(message);
		} catch (MailException exception) {
			log.error("No se pudo enviar confirmacion de compra al cliente para pedido {}", order.getId(), exception);
		}
	}

	private void sendStoreNotification(JavaMailSender mailSender, PurchaseOrder order) {
		if (properties.storeRecipient() == null) {
			return;
		}

		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(properties.storeRecipient());
			message.setFrom(properties.from());
			message.setSubject("Nueva compra registrada - Pedido #" + orderId(order));
			message.setText(buildStoreBody(order));
			mailSender.send(message);
		} catch (MailException exception) {
			log.error("No se pudo enviar notificacion interna de compra para pedido {}", order.getId(), exception);
		}
	}

	private String buildCustomerBody(PurchaseOrder order) {
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
						orderId(order),
						order.getStatus(),
						money(order.getTotal()),
						productLines(order));
	}

	private String buildStoreBody(PurchaseOrder order) {
		return """
				Se registro una nueva compra en TechStore Pro.

				Pedido: #%s
				Estado: %s
				Cliente: %s
				Correo: %s
				Total: S/ %s

				Productos:
				%s
				""".formatted(
						orderId(order),
						order.getStatus(),
						order.getUser().getName(),
						order.getUser().getEmail(),
						money(order.getTotal()),
						productLines(order));
	}

	private String productLines(PurchaseOrder order) {
		List<String> lines = new ArrayList<>();
		for (OrderItem item : order.getItems()) {
			lines.add("- %s x%d | S/ %s".formatted(
					item.getProduct().getName(),
					item.getQuantity(),
					money(item.getSubtotal())));
		}
		return String.join(System.lineSeparator(), lines);
	}

	private String orderId(PurchaseOrder order) {
		if (order.getId() == null) {
			return "pendiente";
		}
		return order.getId().toString();
	}

	private String money(BigDecimal value) {
		return value == null ? "0.00" : value.setScale(2).toPlainString();
	}
}
