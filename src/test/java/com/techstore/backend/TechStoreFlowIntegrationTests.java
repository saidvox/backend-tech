package com.techstore.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class TechStoreFlowIntegrationTests {
	@Autowired
	private MockMvc mockMvc;

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
		mockMvc.perform(get("/productos?q=mouse&category=Mouse&minPrice=50&maxPrice=120&stockStatus=IN_STOCK&page=0&size=10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(1))
				.andExpect(jsonPath("$.content[0].name").value("Mouse Gamer Pro"));
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
