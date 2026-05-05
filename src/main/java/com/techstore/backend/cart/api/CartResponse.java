package com.techstore.backend.cart.api;

import java.math.BigDecimal;
import java.util.List;

import com.techstore.backend.cart.domain.CartItem;

public record CartResponse(List<CartItemResponse> items, BigDecimal total) {
	public static CartResponse from(List<CartItem> items) {
		List<CartItemResponse> responses = items.stream().map(CartItemResponse::from).toList();
		BigDecimal total = responses.stream()
				.map(CartItemResponse::subtotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		return new CartResponse(responses, total);
	}
}
