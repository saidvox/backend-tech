package com.techstore.backend.favorite.application;

import java.util.List;

import com.techstore.backend.config.security.CurrentUserService;
import com.techstore.backend.favorite.domain.FavoriteProduct;
import com.techstore.backend.favorite.infrastructure.FavoriteProductRepository;
import com.techstore.backend.product.api.ProductResponse;
import com.techstore.backend.product.application.ProductService;
import com.techstore.backend.product.domain.Product;
import com.techstore.backend.user.domain.AppUser;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FavoriteService {
	private final FavoriteProductRepository favoriteProductRepository;
	private final ProductService productService;
	private final CurrentUserService currentUserService;

	public FavoriteService(
			FavoriteProductRepository favoriteProductRepository,
			ProductService productService,
			CurrentUserService currentUserService) {
		this.favoriteProductRepository = favoriteProductRepository;
		this.productService = productService;
		this.currentUserService = currentUserService;
	}

	@Transactional(readOnly = true)
	public List<ProductResponse> listCurrentUserFavorites() {
		AppUser user = currentUserService.getCurrentUser();
		return favoriteProductRepository.findByUserAndProductActiveTrueOrderByIdDesc(user).stream()
				.map(FavoriteProduct::getProduct)
				.map(product -> ProductResponse.from(product, true))
				.toList();
	}

	@Transactional
	public FavoriteMutationResult addFavorite(Long productId) {
		AppUser user = currentUserService.getCurrentUser();
		Product product = productService.findEntity(productId);
		boolean exists = favoriteProductRepository.existsByUserAndProduct(user, product);
		if (!exists) {
			favoriteProductRepository.save(new FavoriteProduct(user, product));
		}
		return new FavoriteMutationResult(ProductResponse.from(product, true), !exists);
	}

	@Transactional
	public void removeFavorite(Long productId) {
		AppUser user = currentUserService.getCurrentUser();
		Product product = productService.findEntity(productId);
		favoriteProductRepository.deleteByUserAndProduct(user, product);
	}
}
