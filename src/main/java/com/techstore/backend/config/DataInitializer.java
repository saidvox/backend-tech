package com.techstore.backend.config;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.techstore.backend.product.domain.Product;
import com.techstore.backend.product.infrastructure.ProductRepository;
import com.techstore.backend.user.domain.AppUser;
import com.techstore.backend.user.domain.Role;
import com.techstore.backend.user.infrastructure.UserRepository;

@Configuration
public class DataInitializer {
	@Bean
	CommandLineRunner seedData(UserRepository userRepository, ProductRepository productRepository, PasswordEncoder passwordEncoder) {
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
			if (productRepository.count() == 0) {
				productRepository.save(new Product(
						"Teclado Mecanico RGB",
						"Teclados",
						"Teclado mecanico compacto con switches tactiles e iluminacion RGB.",
						new BigDecimal("189.90"),
						12));
				productRepository.save(new Product(
						"Mouse Gamer Pro",
						"Mouse",
						"Mouse ergonomico de alta precision con sensor optico y botones programables.",
						new BigDecimal("99.90"),
						18));
				productRepository.save(new Product(
						"Audifonos Wireless X",
						"Audifonos",
						"Audifonos inalambricos con microfono, baja latencia y sonido envolvente.",
						new BigDecimal("159.90"),
						10));
			}
		};
	}
}
