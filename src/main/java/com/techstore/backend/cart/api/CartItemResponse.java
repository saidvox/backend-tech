package com.techstore.backend.cart.api;

import java.math.BigDecimal;

import com.techstore.backend.cart.domain.CartItem;

public record CartItemResponse(
		Long productId,
		String productName,
		BigDecimal unitPrice,
		int quantity,
		BigDecimal subtotal,
		int availableStock
) {
	public static CartItemResponse from(CartItem item) {
		BigDecimal unitPrice = item.getProduct().getEffectivePrice();
		BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
		return new CartItemResponse(
				item.getProduct().getId(),
				item.getProduct().getName(),
				unitPrice,
				item.getQuantity(),
				subtotal,
				item.getProduct().getStock());
	}
}
