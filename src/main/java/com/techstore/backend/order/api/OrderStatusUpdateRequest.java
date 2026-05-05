package com.techstore.backend.order.api;

import com.techstore.backend.order.domain.OrderStatus;

import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(
		@NotNull OrderStatus status
) {
}
