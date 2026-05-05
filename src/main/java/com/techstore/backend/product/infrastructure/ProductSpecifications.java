package com.techstore.backend.product.infrastructure;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;

import com.techstore.backend.product.api.ProductSearchCriteria;
import com.techstore.backend.product.api.ProductStockStatus;
import com.techstore.backend.product.domain.Product;

import jakarta.persistence.criteria.JoinType;

import org.springframework.data.jpa.domain.Specification;

public final class ProductSpecifications {
	private static final int LOW_STOCK_THRESHOLD = 5;

	private ProductSpecifications() {
	}

	public static Specification<Product> matching(ProductSearchCriteria criteria, boolean canViewInactive) {
		return Specification.allOf(
				visibility(criteria, canViewInactive),
				textSearch(criteria.q()),
				category(criteria.category()),
				minPrice(criteria.minPrice()),
				maxPrice(criteria.maxPrice()),
				stockStatus(criteria.stockStatus()),
				createdFrom(criteria.createdFrom()),
				createdTo(criteria.createdTo()));
	}

	private static Specification<Product> visibility(ProductSearchCriteria criteria, boolean canViewInactive) {
		return (root, query, builder) -> {
			if (canViewInactive && criteria.active() != null) {
				return builder.equal(root.get("active"), criteria.active());
			}
			if (canViewInactive && criteria.includeInactive()) {
				return builder.conjunction();
			}
			return builder.isTrue(root.get("active"));
		};
	}

	private static Specification<Product> textSearch(String q) {
		return (root, query, builder) -> {
			if (!hasText(q)) {
				return builder.conjunction();
			}
			String pattern = contains(q);
			return builder.or(
					builder.like(builder.lower(root.get("name")), pattern),
					builder.like(builder.lower(root.get("description")), pattern));
		};
	}

	private static Specification<Product> category(String category) {
		return (root, query, builder) -> {
			if (!hasText(category)) {
				return builder.conjunction();
			}
			String normalized = category.trim().toLowerCase();
			return builder.or(
					builder.equal(builder.lower(root.join("category", JoinType.LEFT).get("name")), normalized),
					builder.equal(builder.lower(root.get("categoryName")), normalized));
		};
	}

	private static Specification<Product> minPrice(BigDecimal minPrice) {
		return (root, query, builder) -> minPrice == null
				? builder.conjunction()
				: builder.greaterThanOrEqualTo(root.get("price"), minPrice);
	}

	private static Specification<Product> maxPrice(BigDecimal maxPrice) {
		return (root, query, builder) -> maxPrice == null
				? builder.conjunction()
				: builder.lessThanOrEqualTo(root.get("price"), maxPrice);
	}

	private static Specification<Product> stockStatus(ProductStockStatus stockStatus) {
		return (root, query, builder) -> {
			if (stockStatus == null) {
				return builder.conjunction();
			}
			return switch (stockStatus) {
				case IN_STOCK -> builder.greaterThan(root.get("stock"), 0);
				case OUT_OF_STOCK -> builder.equal(root.get("stock"), 0);
				case LOW_STOCK -> builder.between(root.get("stock"), 1, LOW_STOCK_THRESHOLD);
			};
		};
	}

	private static Specification<Product> createdFrom(LocalDate createdFrom) {
		return (root, query, builder) -> createdFrom == null
				? builder.conjunction()
				: builder.greaterThanOrEqualTo(root.get("createdAt"), createdFrom.atStartOfDay().toInstant(ZoneOffset.UTC));
	}

	private static Specification<Product> createdTo(LocalDate createdTo) {
		return (root, query, builder) -> createdTo == null
				? builder.conjunction()
				: builder.lessThan(root.get("createdAt"), createdTo.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC));
	}

	private static boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private static String contains(String value) {
		return "%" + value.trim().toLowerCase() + "%";
	}
}
