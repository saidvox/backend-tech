package com.techstore.backend.product.api;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductSearchCriteria(
		String q,
		String category,
		BigDecimal minPrice,
		BigDecimal maxPrice,
		ProductStockStatus stockStatus,
		Boolean active,
		boolean includeInactive,
		LocalDate createdFrom,
		LocalDate createdTo
) {
}
