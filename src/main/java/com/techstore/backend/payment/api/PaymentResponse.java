package com.techstore.backend.payment.api;

import java.math.BigDecimal;
import java.time.Instant;

import com.techstore.backend.order.api.OrderResponse;
import com.techstore.backend.payment.domain.Payment;
import com.techstore.backend.payment.domain.PaymentProvider;
import com.techstore.backend.payment.domain.PaymentStatus;

public record PaymentResponse(
		Long id,
		String externalReference,
		String preferenceId,
		String externalPaymentId,
		PaymentProvider provider,
		PaymentStatus status,
		BigDecimal amount,
		String checkoutUrl,
		String initPoint,
		String detail,
		Instant createdAt,
		Instant updatedAt,
		OrderResponse order
) {
	public static PaymentResponse from(Payment payment) {
		return new PaymentResponse(
				payment.getId(),
				payment.getExternalReference(),
				payment.getPreferenceId(),
				payment.getExternalPaymentId(),
				payment.getProvider(),
				payment.getStatus(),
				payment.getAmount(),
				payment.getCheckoutUrl(),
				payment.getInitPoint(),
				payment.getDetail(),
				payment.getCreatedAt(),
				payment.getUpdatedAt(),
				OrderResponse.from(payment.getOrder()));
	}
}
