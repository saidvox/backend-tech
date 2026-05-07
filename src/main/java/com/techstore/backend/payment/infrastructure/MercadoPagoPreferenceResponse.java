package com.techstore.backend.payment.infrastructure;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MercadoPagoPreferenceResponse(
		String id,
		@JsonProperty("init_point") String initPoint,
		@JsonProperty("sandbox_init_point") String sandboxInitPoint
) {
	public String checkoutUrl() {
		return initPoint == null || initPoint.isBlank() ? sandboxInitPoint : initPoint;
	}
}
