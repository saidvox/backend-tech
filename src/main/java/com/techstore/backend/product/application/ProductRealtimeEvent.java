package com.techstore.backend.product.application;

import java.time.Instant;

import com.techstore.backend.product.api.ProductResponse;

public record ProductRealtimeEvent(
		ProductRealtimeEventType type,
		Long productId,
		ProductResponse product,
		Instant occurredAt) {
}
