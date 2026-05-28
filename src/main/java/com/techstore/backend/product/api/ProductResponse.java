package com.techstore.backend.product.api;

import java.math.BigDecimal;
import java.time.Instant;

import com.techstore.backend.product.domain.Product;

public record ProductResponse(
		Long id,
		String name,
		Long categoryId,
		String category,
		String description,
		String imageUrl,
		BigDecimal price,
		BigDecimal offerPrice,
		Instant offerStartsAt,
		Instant offerEndsAt,
		boolean onOffer,
		BigDecimal effectivePrice,
		Integer discountPercentage,
		int stock,
		boolean active,
		boolean favorite
) {
	public static ProductResponse from(Product product) {
		return from(product, false);
	}

	public static ProductResponse from(Product product, boolean favorite) {
		return new ProductResponse(
				product.getId(),
				product.getName(),
				product.getCategoryId(),
				product.getCategory(),
				product.getDescription(),
				product.getImageUrl(),
				product.getPrice(),
				product.getOfferPrice(),
				product.getOfferStartsAt(),
				product.getOfferEndsAt(),
				product.isOnOffer(),
				product.getEffectivePrice(),
				product.getDiscountPercentage(),
				product.getStock(),
				product.isActive(),
				favorite);
	}
}
