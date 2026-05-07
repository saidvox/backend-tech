package com.techstore.backend.payment.domain;

import java.math.BigDecimal;
import java.time.Instant;

import com.techstore.backend.order.domain.PurchaseOrder;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "payments")
public class Payment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "order_id", nullable = false, unique = true)
	private PurchaseOrder order;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private PaymentProvider provider;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private PaymentStatus status = PaymentStatus.PENDING;

	@Column(nullable = false, unique = true, length = 80)
	private String externalReference;

	@Column(length = 120)
	private String preferenceId;

	@Column(length = 120)
	private String externalPaymentId;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal amount;

	@Column(nullable = false, length = 1000)
	private String checkoutUrl;

	@Column(length = 1000)
	private String initPoint;

	@Column(length = 500)
	private String detail;

	@CreatedDate
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@LastModifiedDate
	@Column(nullable = false)
	private Instant updatedAt;

	protected Payment() {
	}

	public Payment(PurchaseOrder order, PaymentProvider provider, String externalReference, String checkoutUrl) {
		this.order = order;
		this.provider = provider;
		this.externalReference = externalReference;
		this.checkoutUrl = checkoutUrl;
		this.initPoint = checkoutUrl;
		this.amount = order.getTotal();
	}

	public Payment(
			PurchaseOrder order,
			PaymentProvider provider,
			String externalReference,
			String checkoutUrl,
			String initPoint,
			String preferenceId) {
		this(order, provider, externalReference, checkoutUrl);
		this.initPoint = initPoint;
		this.preferenceId = preferenceId;
	}

	public void approve() {
		this.status = PaymentStatus.APPROVED;
		this.detail = "Pago aprobado en simulacion de Mercado Pago";
	}

	public void reject(String detail) {
		this.status = PaymentStatus.REJECTED;
		this.detail = detail;
	}

	public void updateFromProvider(PaymentStatus status, String externalPaymentId, String detail) {
		this.status = status;
		this.externalPaymentId = externalPaymentId;
		this.detail = detail;
	}

	public Long getId() {
		return id;
	}

	public PurchaseOrder getOrder() {
		return order;
	}

	public PaymentProvider getProvider() {
		return provider;
	}

	public PaymentStatus getStatus() {
		return status;
	}

	public String getExternalReference() {
		return externalReference;
	}

	public String getPreferenceId() {
		return preferenceId;
	}

	public String getExternalPaymentId() {
		return externalPaymentId;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public String getCheckoutUrl() {
		return checkoutUrl;
	}

	public String getInitPoint() {
		return initPoint;
	}

	public String getDetail() {
		return detail;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
