package com.techstore.backend.product.application;

import java.time.Instant;

import com.techstore.backend.product.api.ProductResponse;
import com.techstore.backend.product.domain.Product;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class ProductRealtimePublisher {
	private static final String PRODUCTS_TOPIC = "/topic/products";

	private final SimpMessagingTemplate messagingTemplate;

	public ProductRealtimePublisher(SimpMessagingTemplate messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}

	public void publishAfterCommit(ProductRealtimeEventType type, Product product) {
		ProductRealtimeEvent event = new ProductRealtimeEvent(
				type,
				product.getId(),
				ProductResponse.from(product),
				Instant.now());

		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			publish(event);
			return;
		}

		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				publish(event);
			}
		});
	}

	private void publish(ProductRealtimeEvent event) {
		messagingTemplate.convertAndSend(PRODUCTS_TOPIC, event);
	}
}
