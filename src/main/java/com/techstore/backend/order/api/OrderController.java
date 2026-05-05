package com.techstore.backend.order.api;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.techstore.backend.config.openapi.OpenApiConfig;
import com.techstore.backend.common.api.PageResponse;
import com.techstore.backend.order.application.OrderService;
import com.techstore.backend.order.domain.OrderStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.security.access.prepost.PreAuthorize;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/pedidos")
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
@Tag(name = "Pedidos", description = "Confirmacion y consulta de pedidos")
public class OrderController {
	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Confirmar pedido desde el carrito")
	public OrderResponse confirmOrder() {
		return orderService.confirmOrder();
	}

	@GetMapping
	@Operation(summary = "Listar pedidos paginados")
	public PageResponse<OrderResponse> orders(
			@Parameter(description = "Usar 'all' para listar todos los pedidos como administrador")
			@RequestParam(defaultValue = "mine") String scope,
			@Parameter(description = "Estado del pedido")
			@RequestParam(required = false) OrderStatus status,
			@Parameter(description = "Busca por nombre de usuario. Solo administradores")
			@RequestParam(required = false) String userName,
			@Parameter(description = "Busca por email de usuario. Solo administradores")
			@RequestParam(required = false) String userEmail,
			@Parameter(description = "Busca pedidos que contengan un producto por nombre")
			@RequestParam(required = false) String productName,
			@Parameter(description = "Busca pedidos que contengan un producto por id")
			@RequestParam(required = false) Long productId,
			@Parameter(description = "Fecha inicial en formato yyyy-MM-dd")
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@Parameter(description = "Fecha final en formato yyyy-MM-dd")
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@Parameter(description = "Total minimo")
			@RequestParam(required = false) BigDecimal minTotal,
			@Parameter(description = "Total maximo")
			@RequestParam(required = false) BigDecimal maxTotal,
			@ParameterObject Pageable pageable) {
		return orderService.findOrders(new OrderSearchCriteria(
				scope,
				status,
				userName,
				userEmail,
				productName,
				productId,
				from,
				to,
				minTotal,
				maxTotal), pageable);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Obtener pedido por id")
	public OrderResponse findById(@PathVariable Long id) {
		return orderService.findById(id);
	}

	@PatchMapping("/{id}/status")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Actualizar estado de pedido")
	public OrderResponse updateStatus(
			@PathVariable Long id,
			@Valid @RequestBody OrderStatusUpdateRequest request) {
		return orderService.updateStatus(id, request);
	}

}
