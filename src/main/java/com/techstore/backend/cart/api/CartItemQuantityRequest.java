package com.techstore.backend.cart.api;

import jakarta.validation.constraints.Min;

public record CartItemQuantityRequest(@Min(1) int quantity) {
}
