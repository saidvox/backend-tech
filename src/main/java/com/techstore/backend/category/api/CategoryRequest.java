package com.techstore.backend.category.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
		@NotBlank @Size(max = 80) String name,
		boolean active
) {
}

