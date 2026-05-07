package com.techstore.backend.product.api;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductRequest(
		@NotBlank @Size(max = 120) String name,
		Long categoryId,
		@Size(max = 80) String category,
		@NotBlank @Size(max = 500) String description,
		@Size(max = 1000) String imageUrl,
		@NotNull @DecimalMin("0.01") BigDecimal price,
		@Min(0) int stock,
		boolean active
) {
	public boolean hasCategoryId() {
		return categoryId != null;
	}

	public String legacyCategoryName() {
		return category == null ? null : category.trim();
	}
}
