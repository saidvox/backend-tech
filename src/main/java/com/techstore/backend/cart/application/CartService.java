package com.techstore.backend.cart.application;

import java.util.List;

import com.techstore.backend.cart.api.CartItemQuantityRequest;
import com.techstore.backend.cart.api.CartResponse;
import com.techstore.backend.cart.domain.CartItem;
import com.techstore.backend.cart.infrastructure.CartItemRepository;
import com.techstore.backend.common.exception.BadRequestException;
import com.techstore.backend.common.exception.ResourceNotFoundException;
import com.techstore.backend.config.security.CurrentUserService;
import com.techstore.backend.product.application.ProductService;
import com.techstore.backend.product.domain.Product;
import com.techstore.backend.user.domain.AppUser;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {
	private final CartItemRepository cartItemRepository;
	private final ProductService productService;
	private final CurrentUserService currentUserService;

	public CartService(CartItemRepository cartItemRepository, ProductService productService, CurrentUserService currentUserService) {
		this.cartItemRepository = cartItemRepository;
		this.productService = productService;
		this.currentUserService = currentUserService;
	}

	@Transactional(readOnly = true)
	public CartResponse getCart() {
		return CartResponse.from(itemsForCurrentUser());
	}

	@Transactional
	public CartResponse upsertItem(Long productId, CartItemQuantityRequest request) {
		AppUser user = currentUserService.getCurrentUser();
		Product product = productService.findEntity(productId);
		validateProduct(product, request.quantity());
		CartItem item = cartItemRepository.findByUserAndProduct(user, product)
				.orElseGet(() -> new CartItem(user, product, 0));
		item.setQuantity(request.quantity());
		cartItemRepository.save(item);
		return CartResponse.from(cartItemRepository.findByUserOrderByIdAsc(user));
	}

	@Transactional
	public void removeItem(Long productId) {
		AppUser user = currentUserService.getCurrentUser();
		Product product = productService.findEntity(productId);
		CartItem item = cartItemRepository.findByUserAndProduct(user, product)
				.orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado en el carrito"));
		cartItemRepository.delete(item);
	}

	@Transactional
	public void clear() {
		cartItemRepository.deleteByUser(currentUserService.getCurrentUser());
	}

	@Transactional(readOnly = true)
	public List<CartItem> itemsForCurrentUser() {
		return cartItemRepository.findByUserOrderByIdAsc(currentUserService.getCurrentUser());
	}

	private void validateProduct(Product product, int quantity) {
		if (!product.isActive()) {
			throw new BadRequestException("El producto no esta disponible");
		}
		if (quantity > product.getStock()) {
			throw new BadRequestException("Stock insuficiente para " + product.getName());
		}
	}
}
