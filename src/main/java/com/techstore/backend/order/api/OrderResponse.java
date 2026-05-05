package com.techstore.backend.order.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.techstore.backend.order.domain.OrderStatus;
import com.techstore.backend.order.domain.PurchaseOrder;
import com.techstore.backend.user.api.UserResponse;

public record OrderResponse(
		Long id,
		UserResponse user,
		OrderStatus status,
		BigDecimal total,
		Instant createdAt,
		List<OrderItemResponse> items
) {
	public static OrderResponse from(PurchaseOrder order) {
		return new OrderResponse(
				order.getId(),
				UserResponse.from(order.getUser()),
				order.getStatus(),
				order.getTotal(),
				order.getCreatedAt(),
				order.getItems().stream().map(OrderItemResponse::from).toList());
	}
}
