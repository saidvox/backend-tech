package com.techstore.backend.payment.infrastructure;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import com.techstore.backend.common.exception.ApiException;
import com.techstore.backend.config.properties.MercadoPagoProperties;
import com.techstore.backend.order.domain.OrderItem;
import com.techstore.backend.order.domain.PurchaseOrder;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClient;

@Component
public class MercadoPagoClient {
	private final MercadoPagoProperties properties;

	public MercadoPagoClient(MercadoPagoProperties properties) {
		this.properties = properties;
	}

	public MercadoPagoPreferenceResponse createPreference(PurchaseOrder order, String externalReference) {
		RestClient client = client();
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("external_reference", externalReference);
		Map<String, String> backUrls = new LinkedHashMap<>();
		if (properties.successUrl() != null) backUrls.put("success", properties.successUrl());
		if (properties.failureUrl() != null) backUrls.put("failure", properties.failureUrl());
		if (properties.pendingUrl() != null) backUrls.put("pending", properties.pendingUrl());
		body.put("back_urls", backUrls);

		body.put("payment_methods", Map.of("installments", 1));
		body.put("items", order.getItems().stream().map(this::item).toList());
		
		// Datos del pagador (payer) - Vital para que MP Sandbox habilite los botones de pago
		Map<String, Object> payer = new LinkedHashMap<>();
		payer.put("email", order.getUser().getEmail());
		String fullName = order.getUser().getName();
		if (fullName != null && !fullName.isBlank()) {
			String[] parts = fullName.split(" ", 2);
			payer.put("first_name", parts[0]);
			if (parts.length > 1) {
				payer.put("last_name", parts[1]);
			}
		}
		body.put("payer", payer);
		
		// Nombre que aparece en el extracto de la tarjeta
		body.put("statement_descriptor", "TECHSTORE");
		
		// Solo activar retorno automático si la URL es PÚBLICA (MP no permite localhost para auto_return)
		if (isPublicUrl(properties.successUrl())) {
			body.put("auto_return", "approved");
		}

		if (isPublicUrl(properties.notificationUrl())) {
			body.put("notification_url", properties.notificationUrl());
		}


		try {

			return client.post()
					.uri("/checkout/preferences")
					.body(body)
					.retrieve()
					.body(MercadoPagoPreferenceResponse.class);
		} catch (RestClientResponseException ex) {
			throw new ApiException(HttpStatus.BAD_GATEWAY, "Mercado Pago rechazo la preferencia: " + ex.getResponseBodyAsString());
		}
	}

	public MercadoPagoPaymentResponse getPayment(String paymentId) {
		return client().get()
				.uri("/v1/payments/{paymentId}", paymentId)
				.retrieve()
				.body(MercadoPagoPaymentResponse.class);
	}

	private Map<String, Object> item(OrderItem item) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("id", item.getProduct().getId().toString());
		body.put("title", item.getProduct().getName());
		body.put("description", item.getProduct().getDescription());
		body.put("category_id", "others");
		body.put("quantity", item.getQuantity());
		body.put("currency_id", "PEN");
		body.put("unit_price", item.getUnitPrice());
		if (isPublicUrl(item.getProduct().getImageUrl())) {
			body.put("picture_url", item.getProduct().getImageUrl());
		}
		return body;
	}

	private boolean isPublicUrl(String value) {
		if (value == null || value.isBlank()) {
			return false;
		}
		String normalized = value.toLowerCase();
		return (normalized.startsWith("http://") || normalized.startsWith("https://"))
				&& !normalized.contains("localhost")
				&& !normalized.contains("127.0.0.1");
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
