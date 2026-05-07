package com.techstore.backend.common.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.techstore.backend.common.exception.BadRequestException;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageableSort {
	private PageableSort() {
	}

	public static Pageable whitelist(Pageable pageable, Map<String, String> allowedSorts, Sort defaultSort) {
		Sort sort = pageable.getSort().isSorted()
				? sanitize(pageable.getSort(), allowedSorts)
				: defaultSort;
		return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
	}

	private static Sort sanitize(Sort requestedSort, Map<String, String> allowedSorts) {
		List<Sort.Order> orders = new ArrayList<>();
		for (Sort.Order order : requestedSort) {
			String property = order.getProperty();
			String mappedProperty = allowedSorts.get(property.toLowerCase(Locale.ROOT));
			if (mappedProperty == null) {
				throw new BadRequestException("Sort no permitido: " + property);
			}
			orders.add(new Sort.Order(order.getDirection(), mappedProperty));
		}
		return Sort.by(orders);
	}
}
