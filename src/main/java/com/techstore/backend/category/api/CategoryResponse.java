package com.techstore.backend.category.api;

import com.techstore.backend.category.domain.Category;

public record CategoryResponse(
		Long id,
		String name,
		boolean active
) {
	public static CategoryResponse from(Category category) {
		return new CategoryResponse(category.getId(), category.getName(), category.isActive());
	}
}

