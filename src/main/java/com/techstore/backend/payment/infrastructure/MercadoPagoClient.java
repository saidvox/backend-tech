package com.techstore.backend.payment.infrastructure;

import java.util.List;
import java.util.Map;

import com.techstore.backend.common.exception.ApiException;
import com.techstore.backend.config.properties.MercadoPagoProperties;
import com.techstore.backend.order.domain.OrderItem;
import com.techstore.backend.order.domain.PurchaseOrder;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class MercadoPagoClient {
	private final MercadoPagoProperties properties;

	public MercadoPagoClient(MercadoPagoProperties properties) {
		this.properties = properties;
	}

	public MercadoPagoPreferenceResponse createPreference(PurchaseOrder order, String externalReference) {
		RestClient client = client();
		Map<String, Object> body = Map.of(
				"auto_return", "approved",
				"external_reference", externalReference,
				"notification_url", properties.notificationUrl(),
				"back_urls", Map.of(
						"success", properties.successUrl(),
						"failure", properties.failureUrl(),
						"pending", properties.pendingUrl()),
				"payer", Map.of(
						"email", order.getUser().getEmail(),
						"name", order.getUser().getName()),
				"items", order.getItems().stream().map(this::item).toList());

		return client.post()
				.uri("/checkout/preferences")
				.body(body)
				.retrieve()
				.body(MercadoPagoPreferenceResponse.class);
	}

	public MercadoPagoPaymentResponse getPayment(String paymentId) {
		return client().get()
				.uri("/v1/payments/{paymentId}", paymentId)
				.retrieve()
				.body(MercadoPagoPaymentResponse.class);
	}

	private Map<String, Object> item(OrderItem item) {
		return Map.of(
				"id", item.getProduct().getId().toString(),
				"title", item.getProduct().getName(),
				"description", item.getProduct().getDescription(),
				"picture_url", item.getProduct().getImageUrl() == null ? "" : item.getProduct().getImageUrl(),
				"category_id", "others",
				"quantity", item.getQuantity(),
				"currency_id", "PEN",
				"unit_price", item.getUnitPrice());
	}

	private RestClient client() {
		if (!properties.hasAccessToken()) {
			throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Mercado Pago no tiene access token configurado");
		}
		String baseUrl = properties.apiBaseUrl() == null || properties.apiBaseUrl().isBlank()
				? "https://api.mercadopago.com"
				: properties.apiBaseUrl();
		return RestClient.builder()
				.baseUrl(baseUrl)
				.defaultHeaders(headers -> {
					headers.setBearerAuth(properties.accessToken());
					headers.setAccept(List.of(MediaType.APPLICATION_JSON));
					headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
				})
				.build();
	}
}
