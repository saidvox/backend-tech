package com.techstore.backend.product.api;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.techstore.backend.config.openapi.OpenApiConfig;
import com.techstore.backend.common.api.PageResponse;
import com.techstore.backend.product.application.ProductService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/productos")
@Tag(name = "Productos", description = "Catalogo publico y administracion de productos")
public class ProductController {
	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping
	@Operation(summary = "Listar productos paginados")
	public PageResponse<ProductResponse> catalog(
			@Parameter(description = "Busca por nombre o descripcion")
			@RequestParam(required = false) String q,
			@Parameter(description = "Filtra por categoria exacta")
			@RequestParam(required = false) String category,
			@Parameter(description = "Precio minimo")
			@RequestParam(required = false) BigDecimal minPrice,
			@Parameter(description = "Precio maximo")
			@RequestParam(required = false) BigDecimal maxPrice,
			@Parameter(description = "Estado de stock: IN_STOCK, OUT_OF_STOCK o LOW_STOCK")
			@RequestParam(required = false) ProductStockStatus stockStatus,
			@Parameter(description = "Filtra activos/inactivos. Solo administradores")
			@RequestParam(required = false) Boolean active,
			@Parameter(description = "Permite a administradores incluir productos inactivos")
			@RequestParam(defaultValue = "false") boolean includeInactive,
			@Parameter(description = "Fecha inicial de creacion en formato yyyy-MM-dd")
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
			@Parameter(description = "Fecha final de creacion en formato yyyy-MM-dd")
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
			@ParameterObject Pageable pageable) {
		return productService.findCatalog(new ProductSearchCriteria(
				q,
				category,
				minPrice,
				maxPrice,
				stockStatus,
				active,
				includeInactive,
				createdFrom,
				createdTo), pageable);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Obtener detalle de producto")
	public ProductResponse detail(@PathVariable Long id) {
		return productService.findById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('ADMIN')")
	@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
	@Operation(summary = "Crear producto")
	public ProductResponse create(@Valid @RequestBody ProductRequest request) {
		return productService.create(request);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
	@Operation(summary = "Actualizar producto")
	public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
		return productService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasRole('ADMIN')")
	@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
	@Operation(summary = "Desactivar producto")
	public void delete(@PathVariable Long id) {
		productService.delete(id);
	}
}
