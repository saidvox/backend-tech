package com.techstore.backend.category.api;

import java.util.List;

import com.techstore.backend.category.application.CategoryService;
import com.techstore.backend.config.openapi.OpenApiConfig;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@RequestMapping("/categorias")
@Tag(name = "Categorias", description = "Catalogo de categorias de productos")
public class CategoryController {
	private final CategoryService categoryService;

	public CategoryController(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	@GetMapping
	@PreAuthorize("!#includeInactive || hasRole('ADMIN')")
	@Operation(summary = "Listar categorias")
	public List<CategoryResponse> list(@RequestParam(defaultValue = "false") boolean includeInactive) {
		return includeInactive ? categoryService.findAll() : categoryService.findActive();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('ADMIN')")
	@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
	@Operation(summary = "Crear categoria")
	public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
		return categoryService.create(request);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
	@Operation(summary = "Actualizar categoria")
	public CategoryResponse update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
		return categoryService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasRole('ADMIN')")
	@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
	@Operation(summary = "Desactivar categoria")
	public void delete(@PathVariable Long id) {
		categoryService.delete(id);
	}
}
