package com.techstore.backend.payment.infrastructure;

import java.util.Optional;

import com.techstore.backend.payment.domain.Payment;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
	@EntityGraph(attributePaths = { "order", "order.user", "order.items", "order.items.product" })
	Optional<Payment> findByExternalReference(String externalReference);

	@EntityGraph(attributePaths = { "order", "order.user", "order.items", "order.items.product" })
	Optional<Payment> findByExternalPaymentId(String externalPaymentId);
}
