package com.techstore.backend.order.api;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.techstore.backend.order.domain.OrderStatus;

public record OrderSearchCriteria(
		String scope,
		OrderStatus status,
		String userName,
		String userEmail,
		String productName,
		Long productId,
		LocalDate from,
		LocalDate to,
		BigDecimal minTotal,
		BigDecimal maxTotal
) {
}
