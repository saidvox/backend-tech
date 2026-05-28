package com.techstore.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techstore.backend.auth.application.OAuth2UserProvisioningService;
import com.techstore.backend.category.infrastructure.CategoryRepository;
import com.techstore.backend.user.infrastructure.UserRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
		"app.datasource.auto-detect=false",
		"spring.profiles.active=test",
		"app.mail.verification.enabled=false",
		"app.mail.purchase.enabled=false"
})
@AutoConfigureMockMvc
class TechStoreFlowIntegrationTests {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private OAuth2UserProvisioningService oauth2UserProvisioningService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	@DisplayName("Should login, add product to cart, and confirm order")
	void shouldConfirmOrderFromCart() throws Exception {
		String token = loginAsCustomer();

		mockMvc.perform(get("/productos?page=0&size=10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(3));

		mockMvc.perform(put("/carrito/items/1")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of("quantity", 2))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total").value(379.80));

		mockMvc.perform(post("/pedidos")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.total").value(379.80))
				.andExpect(jsonPath("$.items.length()").value(1));

		mockMvc.perform(get("/pedidos?page=0&size=10")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(1));
	}

	@Test
	@DisplayName("Should reject cart access without JWT")
	void shouldRejectCartWithoutToken() throws Exception {
		mockMvc.perform(get("/carrito"))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("Should expose Google OAuth2 authorization endpoint")
	void shouldExposeGoogleOAuth2AuthorizationEndpoint() throws Exception {
		mockMvc.perform(get("/oauth2/authorization/google"))
				.andExpect(status().is3xxRedirection());
	}

	@Test
	@DisplayName("Should reject admin product filter without JWT")
	void shouldRejectInactiveProductsWithoutToken() throws Exception {
		mockMvc.perform(get("/productos?includeInactive=true"))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("Should allow admin to list all orders")
	void shouldAllowAdminToListAllOrders() throws Exception {
		String token = login("admin@techstore.com", "admin123");

		mockMvc.perform(get("/pedidos?scope=all&page=0&size=10")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray());
	}

	@Test
	@DisplayName("Should filter products by search, category, price, and stock")
	void shouldFilterProducts() throws Exception {
		Long mouseCategoryId = categoryRepository.findByNameIgnoreCase("Mouse").orElseThrow().getId();

		mockMvc.perform(get("/productos?q=mouse&categoryId={categoryId}&category=Mouse&minPrice=50&maxPrice=120&stockStatus=IN_STOCK&page=0&size=10&sort=price,desc", mouseCategoryId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(1))
				.andExpect(jsonPath("$.content[0].categoryId").value(mouseCategoryId))
				.andExpect(jsonPath("$.content[0].name").value("Mouse Gamer Pro"));
	}

	@Test
	@DisplayName("Should reject unsupported sort fields")
	void shouldRejectUnsupportedSortFields() throws Exception {
		String token = loginAsCustomer();

		mockMvc.perform(get("/productos?page=0&size=10&sort=description,asc"))
				.andExpect(status().isBadRequest());

		mockMvc.perform(get("/pedidos?page=0&size=10&sort=user.email,asc")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("Should filter orders by status, product, user, date, and total")
	void shouldFilterOrders() throws Exception {
		String customerToken = loginAsCustomer();
		String adminToken = login("admin@techstore.com", "admin123");

		mockMvc.perform(put("/carrito/items/2")
						.header("Authorization", "Bearer " + customerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of("quantity", 1))))
				.andExpect(status().isOk());

		mockMvc.perform(post("/pedidos")
						.header("Authorization", "Bearer " + customerToken))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/pedidos?scope=all&status=CONFIRMED&productName=mouse&userEmail=cliente&minTotal=50&maxTotal=120&page=0&size=10")
						.header("Authorization", "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(1))
				.andExpect(jsonPath("$.content[0].user.email").value("cliente@techstore.com"))
				.andExpect(jsonPath("$.content[0].items[0].productName").value("Mouse Gamer Pro"));
	}

	@Test
	@DisplayName("Should allow admin to update order status")
	void shouldAllowAdminToUpdateOrderStatus() throws Exception {
		String customerToken = loginAsCustomer();
		String adminToken = login("admin@techstore.com", "admin123");

		mockMvc.perform(put("/carrito/items/3")
						.header("Authorization", "Bearer " + customerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of("quantity", 1))))
				.andExpect(status().isOk());

		String orderResponse = mockMvc.perform(post("/pedidos")
						.header("Authorization", "Bearer " + customerToken))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();

		long orderId = objectMapper.readTree(orderResponse).get("id").asLong();

		mockMvc.perform(patch("/pedidos/{id}/status", orderId)
						.header("Authorization", "Bearer " + customerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of("status", "CANCELLED"))))
				.andExpect(status().isForbidden());

		mockMvc.perform(patch("/pedidos/{id}/status", orderId)
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of("status", "CANCELLED"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CANCELLED"));
	}

	@Test
	@DisplayName("Should allow customers to manage favorite products")
	void shouldManageFavoriteProducts() throws Exception {
		String token = loginAsCustomer();

		mockMvc.perform(post("/favoritos/1")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.favorite").value(true));

		mockMvc.perform(post("/favoritos/1")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.favorite").value(true));

		mockMvc.perform(get("/favoritos")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[0].favorite").value(true));

		mockMvc.perform(delete("/favoritos/1")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/favoritos")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	@DisplayName("Should expose active offers and use effective price in orders")
	void shouldUseEffectivePriceForActiveOffers() throws Exception {
		String adminToken = login("admin@techstore.com", "admin123");
		String customerToken = loginAsCustomer();
		Long keyboardsCategoryId = categoryRepository.findByNameIgnoreCase("Teclados").orElseThrow().getId();

		mockMvc.perform(put("/productos/1")
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of(
								"name", "Teclado Mecanico RGB",
								"categoryId", keyboardsCategoryId,
								"description", "Teclado mecanico compacto con switches tactiles e iluminacion RGB.",
								"imageUrl", "https://placehold.co/600x400/e2e8f0/0f172a?text=Teclado+RGB",
								"price", "189.90",
								"stock", 12,
								"active", true,
								"offerPrice", "99.90",
								"offerStartsAt", "2026-01-01T00:00:00Z",
								"offerEndsAt", "2099-01-01T00:00:00Z"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.onOffer").value(true))
				.andExpect(jsonPath("$.price").value(189.90))
				.andExpect(jsonPath("$.effectivePrice").value(99.90));

		mockMvc.perform(put("/carrito/items/1")
						.header("Authorization", "Bearer " + customerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of("quantity", 2))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total").value(199.80));

		mockMvc.perform(post("/pedidos")
						.header("Authorization", "Bearer " + customerToken))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.total").value(199.80))
				.andExpect(jsonPath("$.items[0].unitPrice").value(99.90));
	}

	@Test
	@DisplayName("Should link Google OAuth2 profile only when email is already registered")
	void shouldProvisionRegisteredGoogleUser() {
		DefaultOAuth2User googleUser = new DefaultOAuth2User(
				Set.of(),
				Map.of(
						"sub", "google-user-123",
						"email", "cliente@techstore.com",
						"name", "Cliente Google"),
				"sub");

		var user = oauth2UserProvisioningService.findRegisteredGoogleUser(googleUser);
		var sameUser = oauth2UserProvisioningService.findRegisteredGoogleUser(googleUser);

		assertThat(user.getId()).isEqualTo(sameUser.getId());
		assertThat(userRepository.findByEmail("cliente@techstore.com")).isPresent();
		assertThat(user.getOauth2Provider()).isEqualTo("google");
		assertThat(user.getOauth2ProviderId()).isEqualTo("google-user-123");
	}

	@Test
	@DisplayName("Should reject Google OAuth2 profile when email is not registered")
	void shouldRejectUnregisteredGoogleUser() {
		DefaultOAuth2User googleUser = new DefaultOAuth2User(
				Set.of(),
				Map.of(
						"sub", "new-google-user-123",
						"email", "new.google.user@techstore.com",
						"name", "New Google User"),
				"sub");

		assertThatThrownBy(() -> oauth2UserProvisioningService.findRegisteredGoogleUser(googleUser))
				.hasMessageContaining("Primero debes registrarte");
		assertThat(userRepository.findByEmail("new.google.user@techstore.com")).isEmpty();
	}

	@Test
	@DisplayName("Should create user from Google OAuth2 profile when registering")
	void shouldRegisterGoogleUser() {
		DefaultOAuth2User googleUser = new DefaultOAuth2User(
				Set.of(),
				Map.of(
						"sub", "new-google-register-123",
						"email", "new.google.register@techstore.com",
						"name", "New Google Register"),
				"sub");

		var user = oauth2UserProvisioningService.findOrCreateGoogleUserForRegistration(googleUser);

		assertThat(userRepository.findByEmail("new.google.register@techstore.com")).isPresent();
		assertThat(user.getOauth2Provider()).isEqualTo("google");
		assertThat(user.getOauth2ProviderId()).isEqualTo("new-google-register-123");
	}

	private String loginAsCustomer() throws Exception {
		return login("cliente@techstore.com", "cliente123");
	}

	private String login(String email, String password) throws Exception {
		String response = mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of(
								"email", email,
								"password", password))))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		JsonNode json = objectMapper.readTree(response);
		assertThat(json.get("token").asText()).isNotBlank();
		return json.get("token").asText();
	}
}
