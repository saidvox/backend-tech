package com.techstore.backend.payment.infrastructure;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MercadoPagoPaymentResponse(
		String id,
		String status,
		@JsonProperty("external_reference") String externalReference,
		@JsonProperty("status_detail") String statusDetail
) {
}
