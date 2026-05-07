package com.techstore.backend.payment.api;

import com.techstore.backend.order.api.OrderResponse;
import com.techstore.backend.payment.domain.PaymentStatus;

public record CheckoutResponse(
		String externalReference,
		PaymentStatus status,
		String provider,
		String checkoutUrl,
		OrderResponse order
) {
	public static CheckoutResponse from(PaymentResponse payment) {
		return new CheckoutResponse(
				payment.externalReference(),
				payment.status(),
				payment.provider().name(),
				payment.checkoutUrl(),
				payment.order());
	}
}
