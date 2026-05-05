package com.techstore.backend.config.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
	public static final String BEARER_JWT = "bearer-jwt";

	@Bean
	OpenAPI techStoreOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("TechStore Pro API")
						.version("1.0.0")
						.description("API REST para autenticacion, catalogo, carrito persistente y pedidos."))
				.components(new Components()
						.addSecuritySchemes(BEARER_JWT, new SecurityScheme()
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")));
	}
}
