package com.techstore.backend.product.api;

public record ProductImageUploadResponse(
		String fileId,
		String imageUrl
) {
}
