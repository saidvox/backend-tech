package com.techstore.backend.order.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.math.BigDecimal;

import com.techstore.backend.config.properties.PurchaseMailProperties;
import com.techstore.backend.order.domain.OrderItem;
import com.techstore.backend.order.domain.OrderStatus;
import com.techstore.backend.order.domain.PurchaseOrder;
import com.techstore.backend.product.domain.Product;
import com.techstore.backend.user.domain.AppUser;
import com.techstore.backend.user.domain.Role;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderPurchaseEmailSenderTest {

	@Test
	void sendsPurchaseConfirmationToCustomerAndStoreRecipient() {
		JavaMailSender mailSender = mock(JavaMailSender.class);
		@SuppressWarnings("unchecked")
		ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
		when(provider.getIfAvailable()).thenReturn(mailSender);

		PurchaseMailProperties properties = new PurchaseMailProperties(
				true,
				"TechStore Pro <techstoreprotest@gmail.com>",
				"Confirmacion de compra - TechStore Pro",
				"techstoreprotest@gmail.com");
		OrderPurchaseEmailSender sender = new OrderPurchaseEmailSender(provider, properties);

		AppUser user = new AppUser("Cliente Prueba", "cliente@example.com", "secret", Role.USER);
		PurchaseOrder order = new PurchaseOrder(user);
		order.updateStatus(OrderStatus.CONFIRMED);
		order.addItem(new OrderItem(new Product(
				"RTX 5060",
				null,
				"Tarjeta grafica",
				new BigDecimal("5000.00"),
				3,
				null), 2));

		sender.sendPurchaseConfirmation(order);

		ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
		verify(mailSender, times(2)).send(captor.capture());

		assertThat(captor.getAllValues()).hasSize(2);
		assertThat(captor.getAllValues().get(0).getTo()).containsExactly("cliente@example.com");
		assertThat(captor.getAllValues().get(0).getText()).contains("Tu compra en TechStore Pro fue confirmada");
		assertThat(captor.getAllValues().get(1).getTo()).containsExactly("techstoreprotest@gmail.com");
		assertThat(captor.getAllValues().get(1).getSubject()).contains("Nueva compra registrada");
		assertThat(captor.getAllValues().get(1).getText())
				.contains("Cliente: Cliente Prueba")
				.contains("Correo: cliente@example.com")
				.contains("RTX 5060 x2")
				.contains("Total: S/ 10000.00");
	}
}
