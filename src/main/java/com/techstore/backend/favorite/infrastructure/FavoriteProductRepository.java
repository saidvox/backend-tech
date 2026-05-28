package com.techstore.backend.favorite.infrastructure;

import java.util.List;
import java.util.Optional;

import com.techstore.backend.favorite.domain.FavoriteProduct;
import com.techstore.backend.product.domain.Product;
import com.techstore.backend.user.domain.AppUser;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteProductRepository extends JpaRepository<FavoriteProduct, Long> {
	List<FavoriteProduct> findByUserOrderByIdDesc(AppUser user);

	List<FavoriteProduct> findByUserAndProductActiveTrueOrderByIdDesc(AppUser user);

	Optional<FavoriteProduct> findByUserAndProduct(AppUser user, Product product);

	boolean existsByUserAndProduct(AppUser user, Product product);

	void deleteByUserAndProduct(AppUser user, Product product);
}
