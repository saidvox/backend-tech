package com.techstore.backend.product.api;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductRequest(
		@NotBlank @Size(max = 120) String name,
		@NotBlank @Size(max = 80) String category,
		@NotBlank @Size(max = 500) String description,
		@NotNull @DecimalMin("0.01") BigDecimal price,
		@Min(0) int stock,
		boolean active
) {
}
