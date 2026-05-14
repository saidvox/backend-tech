package com.techstore.backend.payment.api;

import java.util.Map;

import com.techstore.backend.config.openapi.OpenApiConfig;
import com.techstore.backend.payment.application.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pagos")
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
@Tag(name = "Pagos", description = "Checkout y pagos con Mercado Pago")
public class PaymentController {
	private final PaymentService paymentService;

	public PaymentController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	@PostMapping("/checkout/mercado-pago")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Crear checkout de Mercado Pago desde el carrito")
	public CheckoutResponse startMercadoPagoCheckout() {
		return paymentService.startMercadoPagoCheckout();
	}

	@PostMapping("/checkout/mercado-pago/simulacion")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Crear checkout de Mercado Pago desde el carrito", deprecated = true)
	public CheckoutResponse startMercadoPagoCheckoutLegacy() {
		return paymentService.startMercadoPagoCheckout();
	}

	@GetMapping("/{externalReference}")
	@Operation(summary = "Obtener estado de pago")
	public PaymentResponse findByExternalReference(@PathVariable String externalReference) {
		return paymentService.findByExternalReference(externalReference);
	}

	@PostMapping("/simulados/{externalReference}/aprobar")
	@Operation(summary = "Aprobar pago simulado")
	public PaymentResponse approveSimulation(@PathVariable String externalReference) {
		return paymentService.approveSimulation(externalReference);
	}

	@PostMapping("/simulados/{externalReference}/rechazar")
	@Operation(summary = "Rechazar pago simulado")
	public PaymentResponse rejectSimulation(@PathVariable String externalReference) {
		return paymentService.rejectSimulation(externalReference);
	}

	@PostMapping("/mercado-pago/{externalReference}/sincronizar")
	@Operation(summary = "Sincronizar retorno de Mercado Pago")
	public PaymentResponse syncMercadoPagoReturn(
			@PathVariable String externalReference,
			@RequestBody MercadoPagoReturnRequest request) {
		return paymentService.syncMercadoPagoReturn(externalReference, request.paymentId(), request.status());
	}

	@PostMapping("/webhooks/mercado-pago")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Recibir webhook de Mercado Pago")
	public void mercadoPagoWebhook(
			@RequestParam(required = false) String topic,
			@RequestParam(required = false) String type,
			@RequestParam(required = false) String id,
			@RequestBody(required = false) Map<String, Object> payload) {
		String paymentId = id == null ? paymentIdFromPayload(payload) : id;
		paymentService.handleMercadoPagoWebhook(topic, type, paymentId);
	}

	private String paymentIdFromPayload(Map<String, Object> payload) {
		if (payload == null) {
			return null;
		}
		Object data = payload.get("data");
		if (data instanceof Map<?, ?> dataMap) {
			Object id = dataMap.get("id");
			return id == null ? null : id.toString();
		}
		Object id = payload.get("id");
		return id == null ? null : id.toString();
	}
}
