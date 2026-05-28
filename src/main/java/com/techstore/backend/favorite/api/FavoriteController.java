package com.techstore.backend.favorite.api;

import java.util.List;

import com.techstore.backend.favorite.application.FavoriteService;
import com.techstore.backend.product.api.ProductResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Favoritos", description = "Productos favoritos del cliente")
public class FavoriteController {
	private final FavoriteService favoriteService;

	public FavoriteController(FavoriteService favoriteService) {
		this.favoriteService = favoriteService;
	}

	@GetMapping("/favoritos")
	public List<ProductResponse> list() {
		return favoriteService.listCurrentUserFavorites();
	}

	@PostMapping("/favoritos/{productId}")
	public ResponseEntity<ProductResponse> add(@PathVariable @Positive Long productId) {
		var result = favoriteService.addFavorite(productId);
		return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK)
				.body(result.product());
	}

	@DeleteMapping("/favoritos/{productId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void remove(@PathVariable @Positive Long productId) {
		favoriteService.removeFavorite(productId);
	}
}
