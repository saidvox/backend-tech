package com.techstore.backend.config;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.techstore.backend.category.domain.Category;
import com.techstore.backend.category.infrastructure.CategoryRepository;
import com.techstore.backend.product.domain.Product;
import com.techstore.backend.product.infrastructure.ProductRepository;
import com.techstore.backend.user.domain.AppUser;
import com.techstore.backend.user.domain.Role;
import com.techstore.backend.user.infrastructure.UserRepository;

@Configuration
public class DataInitializer {
	@Bean
	CommandLineRunner seedData(
			UserRepository userRepository,
			ProductRepository productRepository,
			CategoryRepository categoryRepository,
			PasswordEncoder passwordEncoder) {
		return args -> {
			if (!userRepository.existsByEmail("admin@techstore.com")) {
				userRepository.save(new AppUser(
						"Administrador",
						"admin@techstore.com",
						passwordEncoder.encode("admin123"),
						Role.ADMIN));
			}
			if (!userRepository.existsByEmail("cliente@techstore.com")) {
				userRepository.save(new AppUser(
						"Cliente Demo",
						"cliente@techstore.com",
						passwordEncoder.encode("cliente123"),
						Role.USER));
			}

			Category keyboards = findOrCreateCategory(categoryRepository, "Teclados");
			Category mouse = findOrCreateCategory(categoryRepository, "Mouse");
			Category headphones = findOrCreateCategory(categoryRepository, "Audifonos");

			if (productRepository.count() == 0) {
				productRepository.save(new Product(
						"Teclado Mecanico RGB",
						keyboards,
						"Teclado mecanico compacto con switches tactiles e iluminacion RGB.",
						new BigDecimal("189.90"),
						12,
						"https://placehold.co/600x400/e2e8f0/0f172a?text=Teclado+RGB"));
				productRepository.save(new Product(
						"Mouse Gamer Pro",
						mouse,
						"Mouse ergonomico de alta precision con sensor optico y botones programables.",
						new BigDecimal("99.90"),
						18,
						"https://placehold.co/600x400/dcfce7/14532d?text=Mouse+Gamer"));
				productRepository.save(new Product(
						"Audifonos Wireless X",
						headphones,
						"Audifonos inalambricos con microfono, baja latencia y sonido envolvente.",
						new BigDecimal("159.90"),
						10,
						"https://placehold.co/600x400/dbeafe/1e3a8a?text=Audifonos"));
			} else {
				backfillProductCategories(productRepository, categoryRepository);
			}
		};
	}

	private Category findOrCreateCategory(CategoryRepository categoryRepository, String name) {
		return categoryRepository.findByNameIgnoreCase(name)
				.orElseGet(() -> categoryRepository.save(new Category(name)));
	}

	private void backfillProductCategories(ProductRepository productRepository, CategoryRepository categoryRepository) {
		productRepository.findAll().forEach(product -> {
			String categoryName = product.getCategory();
			Category category = categoryName == null || categoryName.isBlank()
					? null
					: findOrCreateCategory(categoryRepository, categoryName);
			String imageUrl = product.getImageUrl() == null || product.getImageUrl().isBlank()
					? defaultImageUrl(product.getName())
					: product.getImageUrl();
			product.update(
					product.getName(),
					category,
					product.getDescription(),
					product.getPrice(),
					product.getStock(),
					product.isActive(),
					imageUrl);
			productRepository.save(product);
		});
	}

	private String defaultImageUrl(String productName) {
		String label = productName == null || productName.isBlank()
				? "Producto"
				: productName.trim().replace(" ", "+");
		return "https://placehold.co/600x400/f8fafc/334155?text=" + label;
	}
}
