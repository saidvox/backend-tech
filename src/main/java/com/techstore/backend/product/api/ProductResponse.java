package com.techstore.backend.product.api;

import java.math.BigDecimal;

import com.techstore.backend.product.domain.Product;

public record ProductResponse(
		Long id,
		String name,
		Long categoryId,
		String category,
		String description,
		String imageUrl,
		BigDecimal price,
		int stock,
		boolean active
) {
	public static ProductResponse from(Product product) {
		return new ProductResponse(
				product.getId(),
				product.getName(),
				product.getCategoryId(),
				product.getCategory(),
				product.getDescription(),
				product.getImageUrl(),
				product.getPrice(),
				product.getStock(),
				product.isActive());
	}
}
