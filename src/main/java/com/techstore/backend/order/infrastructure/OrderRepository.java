package com.techstore.backend.order.infrastructure;

import java.util.Optional;

import com.techstore.backend.order.domain.PurchaseOrder;
import com.techstore.backend.user.domain.AppUser;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OrderRepository extends JpaRepository<PurchaseOrder, Long>, JpaSpecificationExecutor<PurchaseOrder> {
	@Override
	@EntityGraph(attributePaths = { "user", "items", "items.product" })
	Optional<PurchaseOrder> findById(Long id);

	Page<PurchaseOrder> findByUserOrderByCreatedAtDesc(AppUser user, Pageable pageable);

	Page<PurchaseOrder> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
