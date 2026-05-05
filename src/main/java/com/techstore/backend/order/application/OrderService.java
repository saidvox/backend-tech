package com.techstore.backend.order.application;

import java.util.List;

import com.techstore.backend.cart.application.CartService;
import com.techstore.backend.cart.domain.CartItem;
import com.techstore.backend.cart.infrastructure.CartItemRepository;
import com.techstore.backend.common.api.PageResponse;
import com.techstore.backend.common.exception.ApiException;
import com.techstore.backend.common.exception.BadRequestException;
import com.techstore.backend.common.exception.ResourceNotFoundException;
import com.techstore.backend.config.security.CurrentUserService;
import com.techstore.backend.order.api.OrderResponse;
import com.techstore.backend.order.api.OrderSearchCriteria;
import com.techstore.backend.order.api.OrderStatusUpdateRequest;
import com.techstore.backend.order.domain.OrderItem;
import com.techstore.backend.order.domain.OrderStatus;
import com.techstore.backend.order.domain.PurchaseOrder;
import com.techstore.backend.order.infrastructure.OrderRepository;
import com.techstore.backend.order.infrastructure.OrderSpecifications;
import com.techstore.backend.user.domain.AppUser;
import com.techstore.backend.user.domain.Role;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
	private final OrderRepository orderRepository;
	private final CartItemRepository cartItemRepository;
	private final CartService cartService;
	private final CurrentUserService currentUserService;

	public OrderService(
			OrderRepository orderRepository,
			CartItemRepository cartItemRepository,
			CartService cartService,
			CurrentUserService currentUserService) {
		this.orderRepository = orderRepository;
		this.cartItemRepository = cartItemRepository;
		this.cartService = cartService;
		this.currentUserService = currentUserService;
	}

	@Transactional
	public OrderResponse confirmOrder() {
		AppUser user = currentUserService.getCurrentUser();
		List<CartItem> cartItems = cartService.itemsForCurrentUser();
		if (cartItems.isEmpty()) {
			throw new BadRequestException("El carrito esta vacio");
		}
		for (CartItem item : cartItems) {
			if (!item.getProduct().isActive()) {
				throw new BadRequestException("El producto " + item.getProduct().getName() + " ya no esta disponible");
			}
			if (item.getQuantity() > item.getProduct().getStock()) {
				throw new BadRequestException("Stock insuficiente para " + item.getProduct().getName());
			}
		}

		PurchaseOrder order = new PurchaseOrder(user);
		for (CartItem item : cartItems) {
			order.addItem(new OrderItem(item.getProduct(), item.getQuantity()));
			item.getProduct().reduceStock(item.getQuantity());
		}
		PurchaseOrder savedOrder = orderRepository.save(order);
		cartItemRepository.deleteByUser(user);
		return OrderResponse.from(savedOrder);
	}

	@Transactional(readOnly = true)
	public PageResponse<OrderResponse> findOrders(OrderSearchCriteria criteria, Pageable pageable) {
		AppUser currentUser = currentUserService.getCurrentUser();
		boolean includeAll = "all".equalsIgnoreCase(criteria.scope());
		if (includeAll) {
			if (currentUser.getRole() != Role.ADMIN) {
				throw new ApiException(HttpStatus.FORBIDDEN, "Solo un administrador puede listar todos los pedidos");
			}
		}
		if (!includeAll && currentUser.getRole() != Role.ADMIN && hasUserFilters(criteria)) {
			throw new ApiException(HttpStatus.FORBIDDEN, "Solo un administrador puede filtrar pedidos por usuario");
		}
		validateCriteria(criteria);
		Page<OrderResponse> orders = orderRepository.findAll(
				OrderSpecifications.matching(criteria, currentUser, includeAll),
				pageable).map(OrderResponse::from);
		return PageResponse.from(orders);
	}

	private boolean hasUserFilters(OrderSearchCriteria criteria) {
		return hasText(criteria.userName()) || hasText(criteria.userEmail());
	}

	private void validateCriteria(OrderSearchCriteria criteria) {
		if (criteria.minTotal() != null && criteria.maxTotal() != null
				&& criteria.minTotal().compareTo(criteria.maxTotal()) > 0) {
			throw new BadRequestException("El total minimo no puede ser mayor que el total maximo");
		}
		if (criteria.from() != null && criteria.to() != null && criteria.from().isAfter(criteria.to())) {
			throw new BadRequestException("La fecha inicial no puede ser posterior a la fecha final");
		}
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	@Transactional(readOnly = true)
	public OrderResponse findById(Long id) {
		AppUser currentUser = currentUserService.getCurrentUser();
		PurchaseOrder order = orderRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
		if (currentUser.getRole() != Role.ADMIN && !order.getUser().getId().equals(currentUser.getId())) {
			throw new ResourceNotFoundException("Pedido no encontrado");
		}
		return OrderResponse.from(order);
	}

	@Transactional
	public OrderResponse updateStatus(Long id, OrderStatusUpdateRequest request) {
		AppUser currentUser = currentUserService.getCurrentUser();
		if (currentUser.getRole() != Role.ADMIN) {
			throw new ApiException(HttpStatus.FORBIDDEN, "Solo un administrador puede actualizar pedidos");
		}

		PurchaseOrder order = orderRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
		OrderStatus currentStatus = order.getStatus();
		OrderStatus requestedStatus = request.status();
		if (currentStatus == requestedStatus) {
			return OrderResponse.from(order);
		}

		if (currentStatus == OrderStatus.CONFIRMED && requestedStatus == OrderStatus.CANCELLED) {
			restoreStock(order);
		}
		if (currentStatus == OrderStatus.CANCELLED && requestedStatus == OrderStatus.CONFIRMED) {
			reserveStock(order);
		}

		order.updateStatus(requestedStatus);
		return OrderResponse.from(order);
	}

	private void restoreStock(PurchaseOrder order) {
		for (OrderItem item : order.getItems()) {
			item.getProduct().increaseStock(item.getQuantity());
		}
	}

	private void reserveStock(PurchaseOrder order) {
		for (OrderItem item : order.getItems()) {
			if (!item.getProduct().isActive()) {
				throw new BadRequestException("El producto " + item.getProduct().getName() + " ya no esta disponible");
			}
			if (item.getQuantity() > item.getProduct().getStock()) {
				throw new BadRequestException("Stock insuficiente para " + item.getProduct().getName());
			}
		}
		for (OrderItem item : order.getItems()) {
			item.getProduct().reduceStock(item.getQuantity());
		}
	}
}
