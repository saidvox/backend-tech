package com.techstore.backend.product.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import com.techstore.backend.category.domain.Category;
import com.techstore.backend.config.properties.LowStockMailProperties;
import com.techstore.backend.product.domain.Product;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

class AdminLowStockEmailSenderTest {

	@Test
	void sendsDailyInventoryAlertToAdminWithLowStockAndOutOfStockSections() {
		JavaMailSender mailSender = mock(JavaMailSender.class);
		@SuppressWarnings("unchecked")
		ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
		when(provider.getIfAvailable()).thenReturn(mailSender);
		AdminLowStockEmailSender sender = new AdminLowStockEmailSender(provider, enabledProperties());

		sender.sendDailyAlert(
				List.of(product(10L, "Mouse Gamer", "Mouse", 3)),
				List.of(product(11L, "Monitor 4K", "Monitores", 0)));

		ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
		verify(mailSender).send(captor.capture());

		SimpleMailMessage message = captor.getValue();
		assertThat(message.getTo()).containsExactly("admin@techstore.com");
		assertThat(message.getFrom()).isEqualTo("TechStore Pro <techstoreprotest@gmail.com>");
		assertThat(message.getSubject()).isEqualTo("Alerta de inventario - TechStore Pro");
		assertThat(message.getText())
				.contains("Stock bajo")
				.contains("ID | Producto | Categoria | Stock actual")
				.contains("10 | Mouse Gamer | Mouse | 3")
				.contains("Agotados")
				.contains("11 | Monitor 4K | Monitores | 0");
	}

	@Test
	void doesNotSendWhenThereAreNoProductsToReport() {
		JavaMailSender mailSender = mock(JavaMailSender.class);
		@SuppressWarnings("unchecked")
		ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
		when(provider.getIfAvailable()).thenReturn(mailSender);
		AdminLowStockEmailSender sender = new AdminLowStockEmailSender(provider, enabledProperties());

		sender.sendDailyAlert(List.of(), List.of());

		verify(mailSender, never()).send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));
	}

	@Test
	void doesNotSendWhenMailIsDisabledOrRecipientIsMissing() {
		JavaMailSender mailSender = mock(JavaMailSender.class);
		@SuppressWarnings("unchecked")
		ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
		when(provider.getIfAvailable()).thenReturn(mailSender);
		AdminLowStockEmailSender disabledSender = new AdminLowStockEmailSender(
				provider,
				new LowStockMailProperties(false, "from@example.com", "admin@techstore.com", "Subject", "0 0 8 * * *", 5, true));
		AdminLowStockEmailSender missingRecipientSender = new AdminLowStockEmailSender(
				provider,
				new LowStockMailProperties(true, "from@example.com", " ", "Subject", "0 0 8 * * *", 5, true));

		disabledSender.sendDailyAlert(List.of(product(10L, "Mouse Gamer", "Mouse", 3)), List.of());
		missingRecipientSender.sendDailyAlert(List.of(product(10L, "Mouse Gamer", "Mouse", 3)), List.of());

		verify(mailSender, never()).send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));
	}

	private LowStockMailProperties enabledProperties() {
		return new LowStockMailProperties(
				true,
				"TechStore Pro <techstoreprotest@gmail.com>",
				"admin@techstore.com",
				"Alerta de inventario - TechStore Pro",
				"0 0 8 * * *",
				5,
				true);
	}

	private Product product(Long id, String name, String category, int stock) {
		Product product = new Product(
				name,
				new Category(category),
				"Producto de prueba",
				new BigDecimal("99.90"),
				stock,
				null);
		ReflectionTestUtils.setField(product, "id", id);
		return product;
	}
}
