package com.techstore.backend.cart.api;

import com.techstore.backend.cart.application.CartService;
import com.techstore.backend.config.openapi.OpenApiConfig;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/carrito")
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
@Tag(name = "Carrito", description = "Carrito persistente del usuario autenticado")
public class CartController {
	private final CartService cartService;

	public CartController(CartService cartService) {
		this.cartService = cartService;
	}

	@GetMapping
	@Operation(summary = "Obtener carrito actual")
	public CartResponse getCart() {
		return cartService.getCart();
	}

	@PutMapping("/items/{productId}")
	@Operation(summary = "Crear o reemplazar cantidad de un producto")
	public CartResponse upsertItem(@PathVariable Long productId, @Valid @RequestBody CartItemQuantityRequest request) {
		return cartService.upsertItem(productId, request);
	}

	@DeleteMapping("/items/{productId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Quitar producto del carrito")
	public void removeItem(@PathVariable Long productId) {
		cartService.removeItem(productId);
	}

	@DeleteMapping("/items")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Vaciar carrito")
	public void clear() {
		cartService.clear();
	}
}
