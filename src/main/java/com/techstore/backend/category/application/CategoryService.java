package com.techstore.backend.category.application;

import java.util.List;

import com.techstore.backend.category.api.CategoryRequest;
import com.techstore.backend.category.api.CategoryResponse;
import com.techstore.backend.category.domain.Category;
import com.techstore.backend.category.infrastructure.CategoryRepository;
import com.techstore.backend.common.exception.ApiException;
import com.techstore.backend.common.exception.ResourceNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {
	private final CategoryRepository categoryRepository;

	public CategoryService(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}

	@Transactional(readOnly = true)
	public List<CategoryResponse> findActive() {
		return categoryRepository.findByActiveTrueOrderByNameAsc().stream()
				.map(CategoryResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<CategoryResponse> findAll() {
		return categoryRepository.findAll().stream()
				.map(CategoryResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public Category findEntity(Long id) {
		return categoryRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada"));
	}

	@Transactional
	public CategoryResponse create(CategoryRequest request) {
		String name = request.name().trim();
		if (categoryRepository.existsByNameIgnoreCase(name)) {
			throw new ApiException(HttpStatus.CONFLICT, "La categoria ya existe");
		}
		Category category = new Category(name);
		category.update(name, request.active());
		return CategoryResponse.from(categoryRepository.save(category));
	}

	@Transactional
	public CategoryResponse update(Long id, CategoryRequest request) {
		Category category = findEntity(id);
		String name = request.name().trim();
		categoryRepository.findByNameIgnoreCase(name)
				.filter(existing -> !existing.getId().equals(id))
				.ifPresent(existing -> {
					throw new ApiException(HttpStatus.CONFLICT, "La categoria ya existe");
				});
		category.update(name, request.active());
		return CategoryResponse.from(category);
	}

	@Transactional
	public void delete(Long id) {
		Category category = findEntity(id);
		category.update(category.getName(), false);
	}
}

