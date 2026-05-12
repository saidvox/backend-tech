package com.techstore.backend.payment.api;

public record MercadoPagoReturnRequest(
		String paymentId,
		String status
) {
}
