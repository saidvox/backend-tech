package com.techstore.backend.cart.infrastructure;

import java.util.List;
import java.util.Optional;

import com.techstore.backend.cart.domain.CartItem;
import com.techstore.backend.product.domain.Product;
import com.techstore.backend.user.domain.AppUser;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
	List<CartItem> findByUserOrderByIdAsc(AppUser user);

	Optional<CartItem> findByUserAndProduct(AppUser user, Product product);

	void deleteByUser(AppUser user);
}
