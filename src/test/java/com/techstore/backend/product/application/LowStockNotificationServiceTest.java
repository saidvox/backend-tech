package com.techstore.backend.product.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import com.techstore.backend.category.domain.Category;
import com.techstore.backend.config.properties.LowStockMailProperties;
import com.techstore.backend.product.domain.Product;
import com.techstore.backend.product.infrastructure.ProductRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LowStockNotificationServiceTest {
	@Mock
	private ProductRepository productRepository;

	@Mock
	private AdminLowStockEmailSender emailSender;

	@Test
	void sendsDailyNotificationWithLowStockAndOutOfStockProducts() {
		Product lowStockProduct = product("Mouse Gamer", "Mouse", 3);
		Product outOfStockProduct = product("Monitor 4K", "Monitores", 0);
		when(productRepository.findByActiveTrueAndStockBetweenOrderByStockAscNameAsc(1, 5))
				.thenReturn(List.of(lowStockProduct));
		when(productRepository.findByActiveTrueAndStockOrderByNameAsc(0))
				.thenReturn(List.of(outOfStockProduct));
		when(emailSender.sendDailyAlert(List.of(lowStockProduct), List.of(outOfStockProduct))).thenReturn(true);

		int sent = service(enabledProperties()).sendDailyLowStockNotifications();

		assertThat(sent).isEqualTo(1);
		verify(emailSender).sendDailyAlert(List.of(lowStockProduct), List.of(outOfStockProduct));
	}

	@Test
	void doesNotSendWhenThereAreNoProductsToReport() {
		when(productRepository.findByActiveTrueAndStockBetweenOrderByStockAscNameAsc(1, 5))
				.thenReturn(List.of());
		when(productRepository.findByActiveTrueAndStockOrderByNameAsc(0))
				.thenReturn(List.of());

		int sent = service(enabledProperties()).sendDailyLowStockNotifications();

		assertThat(sent).isZero();
		verify(emailSender, never()).sendDailyAlert(org.mockito.ArgumentMatchers.anyList(), org.mockito.ArgumentMatchers.anyList());
	}

	@Test
	void skipsOutOfStockQueryWhenConfiguredOff() {
		Product lowStockProduct = product("Mouse Gamer", "Mouse", 3);
		when(productRepository.findByActiveTrueAndStockBetweenOrderByStockAscNameAsc(1, 5))
				.thenReturn(List.of(lowStockProduct));
		LowStockMailProperties properties = new LowStockMailProperties(
				true,
				"from@example.com",
				"admin@techstore.com",
				"Subject",
				"0 0 8 * * *",
				5,
				false);
		when(emailSender.sendDailyAlert(List.of(lowStockProduct), List.of())).thenReturn(true);

		int sent = service(properties).sendDailyLowStockNotifications();

		assertThat(sent).isEqualTo(1);
		verify(productRepository, never()).findByActiveTrueAndStockOrderByNameAsc(0);
		verify(emailSender).sendDailyAlert(List.of(lowStockProduct), List.of());
	}

	private LowStockNotificationService service(LowStockMailProperties properties) {
		return new LowStockNotificationService(properties, productRepository, emailSender);
	}

	private LowStockMailProperties enabledProperties() {
		return new LowStockMailProperties(
				true,
				"from@example.com",
				"admin@techstore.com",
				"Subject",
				"0 0 8 * * *",
				5,
				true);
	}

	private Product product(String name, String category, int stock) {
		return new Product(
				name,
				new Category(category),
				"Producto de prueba",
				new BigDecimal("99.90"),
				stock,
				null);
	}
}
