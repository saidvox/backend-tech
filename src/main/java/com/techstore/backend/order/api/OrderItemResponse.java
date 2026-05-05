package com.techstore.backend.order.api;

import java.math.BigDecimal;

import com.techstore.backend.order.domain.OrderItem;

public record OrderItemResponse(
		Long productId,
		String productName,
		int quantity,
		BigDecimal unitPrice,
		BigDecimal subtotal
) {
	public static OrderItemResponse from(OrderItem item) {
		return new OrderItemResponse(
				item.getProduct().getId(),
				item.getProduct().getName(),
				item.getQuantity(),
				item.getUnitPrice(),
				item.getSubtotal());
	}
}
