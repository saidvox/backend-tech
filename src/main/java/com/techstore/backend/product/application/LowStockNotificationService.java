package com.techstore.backend.product.application;

import java.util.List;

import com.techstore.backend.config.properties.LowStockMailProperties;
import com.techstore.backend.product.domain.Product;
import com.techstore.backend.product.infrastructure.ProductRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LowStockNotificationService {
	private final LowStockMailProperties properties;
	private final ProductRepository productRepository;
	private final AdminLowStockEmailSender emailSender;

	public LowStockNotificationService(
			LowStockMailProperties properties,
			ProductRepository productRepository,
			AdminLowStockEmailSender emailSender) {
		this.properties = properties;
		this.productRepository = productRepository;
		this.emailSender = emailSender;
	}

	@Scheduled(cron = "${app.mail.low-stock.cron:0 0 8 * * *}", zone = "America/Lima")
	@Transactional(readOnly = true)
	public int sendDailyLowStockNotifications() {
		if (!properties.enabled()) {
			return 0;
		}

		List<Product> lowStockProducts = productRepository.findByActiveTrueAndStockBetweenOrderByStockAscNameAsc(1, properties.threshold());
		List<Product> outOfStockProducts = properties.includeOutOfStock()
				? productRepository.findByActiveTrueAndStockOrderByNameAsc(0)
				: List.of();

		if (lowStockProducts.isEmpty() && outOfStockProducts.isEmpty()) {
			return 0;
		}

		return emailSender.sendDailyAlert(lowStockProducts, outOfStockProducts) ? 1 : 0;
	}
}
