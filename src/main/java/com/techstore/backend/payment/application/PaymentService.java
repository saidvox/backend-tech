package com.techstore.backend.payment.application;

import java.util.UUID;

import com.techstore.backend.common.exception.ApiException;
import com.techstore.backend.common.exception.ResourceNotFoundException;
import com.techstore.backend.config.properties.MercadoPagoProperties;
import com.techstore.backend.config.security.CurrentUserService;
import com.techstore.backend.order.domain.PurchaseOrder;
import com.techstore.backend.order.application.OrderService;
import com.techstore.backend.payment.api.CheckoutResponse;
import com.techstore.backend.payment.api.PaymentResponse;
import com.techstore.backend.payment.domain.Payment;
import com.techstore.backend.payment.domain.PaymentProvider;
import com.techstore.backend.payment.domain.PaymentStatus;
import com.techstore.backend.payment.infrastructure.MercadoPagoClient;
import com.techstore.backend.payment.infrastructure.MercadoPagoPaymentResponse;
import com.techstore.backend.payment.infrastructure.MercadoPagoPreferenceResponse;
import com.techstore.backend.payment.infrastructure.PaymentRepository;
import com.techstore.backend.user.domain.AppUser;
import com.techstore.backend.user.domain.Role;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class PaymentService {
	private final PaymentRepository paymentRepository;
	private final OrderService orderService;
	private final CurrentUserService currentUserService;
	private final MercadoPagoClient mercadoPagoClient;
	private final MercadoPagoProperties mercadoPagoProperties;

	public PaymentService(
			PaymentRepository paymentRepository,
			OrderService orderService,
			CurrentUserService currentUserService,
			MercadoPagoClient mercadoPagoClient,
			MercadoPagoProperties mercadoPagoProperties) {
		this.paymentRepository = paymentRepository;
		this.orderService = orderService;
		this.currentUserService = currentUserService;
		this.mercadoPagoClient = mercadoPagoClient;
		this.mercadoPagoProperties = mercadoPagoProperties;
	}

	@Transactional
	public CheckoutResponse startMercadoPagoSimulation() {
		PurchaseOrder order = orderService.createPendingPaymentOrderFromCart();
		String externalReference = "MP-SIM-" + UUID.randomUUID();
		if (mercadoPagoProperties.realMode()) {
			return startRealMercadoPagoCheckout(order, externalReference);
		}
		return startSimulatedMercadoPagoCheckout(order, externalReference);
	}

	@Transactional
	public void handleMercadoPagoWebhook(String topic, String type, String paymentId) {
		if (!mercadoPagoProperties.realMode() || paymentId == null || paymentId.isBlank()) {
			return;
		}
		String eventType = type == null || type.isBlank() ? topic : type;
		if (eventType == null || !eventType.toLowerCase().contains("payment")) {
			return;
		}
		MercadoPagoPaymentResponse providerPayment = mercadoPagoClient.getPayment(paymentId);
		if (providerPayment == null || providerPayment.externalReference() == null) {
			return;
		}
		Payment payment = paymentRepository.findByExternalReference(providerPayment.externalReference())
				.orElseGet(() -> paymentRepository.findByExternalPaymentId(paymentId)
						.orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado")));
		PaymentStatus status = mapStatus(providerPayment.status());
		payment.updateFromProvider(status, providerPayment.id(), providerPayment.statusDetail());
		syncOrderStatus(payment);
	}

	private CheckoutResponse startSimulatedMercadoPagoCheckout(PurchaseOrder order, String externalReference) {
		String checkoutUrl = UriComponentsBuilder
				.fromUriString(mercadoPagoProperties.simulatedCheckoutUrl())
				.queryParam("externalReference", externalReference)
				.build()
				.toUriString();
		Payment payment = paymentRepository.save(new Payment(
				order,
				PaymentProvider.MERCADO_PAGO_SIMULATED,
				externalReference,
				checkoutUrl));
		return CheckoutResponse.from(PaymentResponse.from(payment));
	}

	private CheckoutResponse startRealMercadoPagoCheckout(PurchaseOrder order, String externalReference) {
		MercadoPagoPreferenceResponse preference = mercadoPagoClient.createPreference(order, externalReference);
		String checkoutUrl = preference.checkoutUrl(mercadoPagoProperties.sandboxMode());
		Payment payment = paymentRepository.save(new Payment(
				order,
				PaymentProvider.MERCADO_PAGO,
				externalReference,
				checkoutUrl,
				preference.initPoint(),
				preference.id()));
		return CheckoutResponse.from(PaymentResponse.from(payment));
	}

	@Transactional(readOnly = true)
	public PaymentResponse findByExternalReference(String externalReference) {
		Payment payment = findPayment(externalReference);
		assertCanAccess(payment);
		return PaymentResponse.from(payment);
	}

	@Transactional
	public PaymentResponse approveSimulation(String externalReference) {
		Payment payment = findPayment(externalReference);
		assertCanAccess(payment);
		assertPending(payment);
		payment.approve();
		orderService.markPaymentApproved(payment.getOrder());
		return PaymentResponse.from(payment);
	}

	@Transactional
	public PaymentResponse rejectSimulation(String externalReference) {
		Payment payment = findPayment(externalReference);
		assertCanAccess(payment);
		assertPending(payment);
		payment.reject("Pago rechazado en simulacion de Mercado Pago");
		orderService.markPaymentRejected(payment.getOrder());
		return PaymentResponse.from(payment);
	}

	@Transactional
	public PaymentResponse syncMercadoPagoReturn(String externalReference, String paymentId, String providerStatus) {
		Payment payment = findPayment(externalReference);
		assertCanAccess(payment);
		if (payment.getProvider() != PaymentProvider.MERCADO_PAGO) {
			return PaymentResponse.from(payment);
		}
		if (hasText(paymentId) && !"null".equalsIgnoreCase(paymentId)) {
			MercadoPagoPaymentResponse providerPayment = mercadoPagoClient.getPayment(paymentId);
			PaymentStatus status = mapStatus(providerPayment.status());
			payment.updateFromProvider(status, providerPayment.id(), providerPayment.statusDetail());
		} else if (hasText(providerStatus)) {
			payment.updateFromProvider(mapStatus(providerStatus), null, providerStatus);
		}
		syncOrderStatus(payment);
		return PaymentResponse.from(payment);
	}

	private void syncOrderStatus(Payment payment) {
		if (payment.getStatus() == PaymentStatus.APPROVED && payment.getOrder().getStatus().name().equals("PENDING_PAYMENT")) {
			orderService.markPaymentApproved(payment.getOrder());
		}
		if ((payment.getStatus() == PaymentStatus.REJECTED || payment.getStatus() == PaymentStatus.CANCELLED)
				&& payment.getOrder().getStatus().name().equals("PENDING_PAYMENT")) {
			orderService.markPaymentRejected(payment.getOrder());
		}
	}

	private PaymentStatus mapStatus(String providerStatus) {
		if (providerStatus == null) {
			return PaymentStatus.PENDING;
		}
		return switch (providerStatus.toLowerCase()) {
			case "approved", "accredited" -> PaymentStatus.APPROVED;
			case "rejected" -> PaymentStatus.REJECTED;
			case "cancelled", "refunded", "charged_back" -> PaymentStatus.CANCELLED;
			default -> PaymentStatus.PENDING;
		};
	}

	private Payment findPayment(String externalReference) {
		return paymentRepository.findByExternalReference(externalReference)
				.orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado"));
	}

	private boolean hasText(String value) {
		return value != null && !value.isBlank();
	}

	private void assertPending(Payment payment) {
		if (payment.getStatus() != PaymentStatus.PENDING) {
			throw new ApiException(HttpStatus.CONFLICT, "El pago ya fue procesado");
		}
	}

	private void assertCanAccess(Payment payment) {
		AppUser currentUser = currentUserService.getCurrentUser();
		if (currentUser.getRole() != Role.ADMIN && !payment.getOrder().getUser().getId().equals(currentUser.getId())) {
			throw new ResourceNotFoundException("Pago no encontrado");
		}
	}
}
