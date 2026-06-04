package com.techstore.backend.product.infrastructure;

import java.util.List;

import com.techstore.backend.product.domain.Product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
	List<Product> findByActiveTrueAndStockBetweenOrderByStockAscNameAsc(int minStock, int maxStock);

	List<Product> findByActiveTrueAndStockOrderByNameAsc(int stock);
}
