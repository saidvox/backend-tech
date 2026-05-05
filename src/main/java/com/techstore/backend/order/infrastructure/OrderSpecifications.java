package com.techstore.backend.order.infrastructure;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;

import com.techstore.backend.order.api.OrderSearchCriteria;
import com.techstore.backend.order.domain.PurchaseOrder;
import com.techstore.backend.user.domain.AppUser;

import jakarta.persistence.criteria.JoinType;

import org.springframework.data.jpa.domain.Specification;

public final class OrderSpecifications {
	private OrderSpecifications() {
	}

	public static Specification<PurchaseOrder> matching(OrderSearchCriteria criteria, AppUser currentUser, boolean includeAll) {
		return Specification.allOf(
				visibility(currentUser, includeAll),
				status(criteria),
				userName(criteria.userName()),
				userEmail(criteria.userEmail()),
				productName(criteria.productName()),
				productId(criteria.productId()),
				from(criteria.from()),
				to(criteria.to()),
				minTotal(criteria.minTotal()),
				maxTotal(criteria.maxTotal()));
	}

	private static Specification<PurchaseOrder> visibility(AppUser currentUser, boolean includeAll) {
		return (root, query, builder) -> includeAll
				? builder.conjunction()
				: builder.equal(root.get("user"), currentUser);
	}

	private static Specification<PurchaseOrder> status(OrderSearchCriteria criteria) {
		return (root, query, builder) -> criteria.status() == null
				? builder.conjunction()
				: builder.equal(root.get("status"), criteria.status());
	}

	private static Specification<PurchaseOrder> userName(String userName) {
		return (root, query, builder) -> {
			if (!hasText(userName)) {
				return builder.conjunction();
			}
			return builder.like(builder.lower(root.join("user", JoinType.INNER).get("name")), contains(userName));
		};
	}

	private static Specification<PurchaseOrder> userEmail(String userEmail) {
		return (root, query, builder) -> {
			if (!hasText(userEmail)) {
				return builder.conjunction();
			}
			return builder.like(builder.lower(root.join("user", JoinType.INNER).get("email")), contains(userEmail));
		};
	}

	private static Specification<PurchaseOrder> productName(String productName) {
		return (root, query, builder) -> {
			if (!hasText(productName)) {
				return builder.conjunction();
			}
			query.distinct(true);
			var product = root.join("items", JoinType.INNER).join("product", JoinType.INNER);
			return builder.like(builder.lower(product.get("name")), contains(productName));
		};
	}

	private static Specification<PurchaseOrder> productId(Long productId) {
		return (root, query, builder) -> {
			if (productId == null) {
				return builder.conjunction();
			}
			query.distinct(true);
			var product = root.join("items", JoinType.INNER).join("product", JoinType.INNER);
			return builder.equal(product.get("id"), productId);
		};
	}

	private static Specification<PurchaseOrder> from(LocalDate from) {
		return (root, query, builder) -> from == null
				? builder.conjunction()
				: builder.greaterThanOrEqualTo(root.get("createdAt"), from.atStartOfDay().toInstant(ZoneOffset.UTC));
	}

	private static Specification<PurchaseOrder> to(LocalDate to) {
		return (root, query, builder) -> to == null
				? builder.conjunction()
				: builder.lessThan(root.get("createdAt"), to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC));
	}

	private static Specification<PurchaseOrder> minTotal(BigDecimal minTotal) {
		return (root, query, builder) -> minTotal == null
				? builder.conjunction()
				: builder.greaterThanOrEqualTo(root.get("total"), minTotal);
	}

	private static Specification<PurchaseOrder> maxTotal(BigDecimal maxTotal) {
		return (root, query, builder) -> maxTotal == null
				? builder.conjunction()
				: builder.lessThanOrEqualTo(root.get("total"), maxTotal);
	}

	private static boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private static String contains(String value) {
		return "%" + value.trim().toLowerCase() + "%";
	}
}
