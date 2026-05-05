package com.techstore.backend.category.infrastructure;

import java.util.List;
import java.util.Optional;

import com.techstore.backend.category.domain.Category;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
	boolean existsByNameIgnoreCase(String name);

	Optional<Category> findByNameIgnoreCase(String name);

	List<Category> findByActiveTrueOrderByNameAsc();
}

