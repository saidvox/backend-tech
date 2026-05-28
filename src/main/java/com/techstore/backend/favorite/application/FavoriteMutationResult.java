package com.techstore.backend.favorite.application;

import com.techstore.backend.product.api.ProductResponse;

public record FavoriteMutationResult(ProductResponse product, boolean created) {
}
