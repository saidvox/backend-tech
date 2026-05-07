package com.techstore.backend.product.application;

import java.util.Map;

import com.techstore.backend.common.exception.ApiException;
import com.techstore.backend.common.exception.BadRequestException;
import com.techstore.backend.common.exception.ResourceNotFoundException;
import com.techstore.backend.common.api.PageResponse;
import com.techstore.backend.common.api.PageableSort;
import com.techstore.backend.config.security.CurrentUserService;
import com.techstore.backend.category.domain.Category;
import com.techstore.backend.category.infrastructure.CategoryRepository;
import com.techstore.backend.product.api.ProductRequest;
import com.techstore.backend.product.api.ProductResponse;
import com.techstore.backend.product.api.ProductSearchCriteria;
import com.techstore.backend.product.domain.Product;
import com.techstore.backend.product.infrastructure.ProductRepository;
import com.techstore.backend.product.infrastructure.ProductSpecifications;
import com.techstore.backend.user.domain.Role;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
	private static final Map<String, String> ALLOWED_SORTS = Map.of(
			"id", "id",
			"name", "name",
			"price", "price",
			"stock", "stock",
			"active", "active",
			"createdat", "createdAt",
			"category", "categoryName");
	private static final Sort DEFAULT_SORT = Sort.by(
			Sort.Order.desc("createdAt"),
			Sort.Order.desc("id"));

	private final ProductRepository productRepository;
	private final CategoryRepository categoryRepository;
	private final CurrentUserService currentUserService;

	public ProductService(
			ProductRepository productRepository,
			CategoryRepository categoryRepository,
			CurrentUserService currentUserService) {
		this.productRepository = productRepository;
		this.categoryRepository = categoryRepository;
		this.currentUserService = currentUserService;
	}

	@Transactional(readOnly = true)
	public PageResponse<ProductResponse> findCatalog(ProductSearchCriteria criteria, Pageable pageable) {
		boolean canViewInactive = false;
		if (criteria.includeInactive() || criteria.active() != null) {
			var currentUser = currentUserService.getCurrentUserOrNull();
			if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
				throw new ApiException(HttpStatus.FORBIDDEN, "Solo un administrador puede filtrar productos inactivos");
			}
			canViewInactive = true;
		}
		validateCriteria(criteria);
		Pageable sanitizedPageable = PageableSort.whitelist(pageable, ALLOWED_SORTS, DEFAULT_SORT);
		Page<ProductResponse> products = productRepository.findAll(
				ProductSpecifications.matching(criteria, canViewInactive),
				sanitizedPageable).map(ProductResponse::from);
		return PageResponse.from(products);
	}

	private void validateCriteria(ProductSearchCriteria criteria) {
		if (criteria.minPrice() != null && criteria.maxPrice() != null
				&& criteria.minPrice().compareTo(criteria.maxPrice()) > 0) {
			throw new BadRequestException("El precio minimo no puede ser mayor que el precio maximo");
		}
		if (criteria.createdFrom() != null && criteria.createdTo() != null
				&& criteria.createdFrom().isAfter(criteria.createdTo())) {
			throw new BadRequestException("La fecha inicial no puede ser posterior a la fecha final");
		}
	}

	@Transactional(readOnly = true)
	public Product findEntity(Long id) {
		return productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
	}

	@Transactional(readOnly = true)
	public ProductResponse findById(Long id) {
		return ProductResponse.from(findEntity(id));
	}

	@Transactional
	public ProductResponse create(ProductRequest request) {
		Category category = resolveCategory(request);
		Product product = new Product(
				request.name().trim(),
				category,
				request.description().trim(),
				request.price(),
				request.stock(),
				trimToNull(request.imageUrl()));
		product.update(product.getName(), category, product.getDescription(), product.getPrice(), product.getStock(), request.active(), product.getImageUrl());
		return ProductResponse.from(productRepository.save(product));
	}

	@Transactional
	public ProductResponse update(Long id, ProductRequest request) {
		Product product = findEntity(id);
		Category category = resolveCategory(request);
		product.update(
				request.name().trim(),
				category,
				request.description().trim(),
				request.price(),
				request.stock(),
				request.active(),
				trimToNull(request.imageUrl()));
		return ProductResponse.from(product);
	}

	@Transactional
	public void delete(Long id) {
		Product product = findEntity(id);
		Category category = product.getCategoryId() == null ? null : categoryRepository.findById(product.getCategoryId()).orElse(null);
		product.update(product.getName(), category, product.getDescription(), product.getPrice(), product.getStock(), false, product.getImageUrl());
	}

	private Category resolveCategory(ProductRequest request) {
		if (request.hasCategoryId()) {
			return categoryRepository.findById(request.categoryId())
					.orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada"));
		}
		String legacyCategoryName = request.legacyCategoryName();
		if (legacyCategoryName == null || legacyCategoryName.isBlank()) {
			throw new BadRequestException("Debe seleccionar una categoria");
		}
		return categoryRepository.findByNameIgnoreCase(legacyCategoryName)
				.orElseGet(() -> categoryRepository.save(new Category(legacyCategoryName)));
	}

	private String trimToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
