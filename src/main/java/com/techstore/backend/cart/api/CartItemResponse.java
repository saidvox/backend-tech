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
		BigDecimal subtotal = item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
		return new CartItemResponse(
				item.getProduct().getId(),
				item.getProduct().getName(),
				item.getProduct().getPrice(),
				item.getQuantity(),
				subtotal,
				item.getProduct().getStock());
	}
}
