package com.techstore.backend.order.application;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import com.techstore.backend.cart.application.CartService;
import com.techstore.backend.cart.domain.CartItem;
import com.techstore.backend.cart.infrastructure.CartItemRepository;
import com.techstore.backend.category.domain.Category;
import com.techstore.backend.config.security.CurrentUserService;
import com.techstore.backend.order.domain.PurchaseOrder;
import com.techstore.backend.order.infrastructure.OrderRepository;
import com.techstore.backend.product.application.ProductRealtimeEventType;
import com.techstore.backend.product.application.ProductRealtimePublisher;
import com.techstore.backend.product.domain.Product;
import com.techstore.backend.user.domain.AppUser;
import com.techstore.backend.user.domain.Role;
import com.techstore.backend.order.domain.OrderStatus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
	@Mock
	private OrderRepository orderRepository;

	@Mock
	private CartItemRepository cartItemRepository;

	@Mock
	private CartService cartService;

	@Mock
	private CurrentUserService currentUserService;

	@Mock
	private OrderPurchaseEmailSender purchaseEmailSender;

	@Mock
	private ProductRealtimePublisher productRealtimePublisher;

	@Captor
	private ArgumentCaptor<PurchaseOrder> orderCaptor;

	@Test
	void confirmOrderSendsPurchaseNotificationToCustomer() {
		AppUser user = new AppUser("Cliente Demo", "cliente@example.com", "encoded", Role.USER);
		Product product = new Product(
				"Mouse Gamer Pro",
				new Category("Mouse"),
				"Mouse ergonomico",
				new BigDecimal("99.90"),
				10,
				"https://example.com/mouse.png");
		CartItem cartItem = new CartItem(user, product, 2);
		when(currentUserService.getCurrentUser()).thenReturn(user);
		when(cartService.itemsForCurrentUser()).thenReturn(List.of(cartItem));
		when(orderRepository.save(orderCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

		orderService().confirmOrder();

		verify(purchaseEmailSender).sendPurchaseConfirmation(orderCaptor.getValue());
		verify(productRealtimePublisher).publishAfterCommit(ProductRealtimeEventType.PRODUCT_STOCK_CHANGED, product);
	}

	@Test
	void markPaymentApprovedSendsPurchaseNotificationToCustomer() {
		AppUser user = new AppUser("Cliente Demo", "cliente@example.com", "encoded", Role.USER);
		PurchaseOrder order = new PurchaseOrder(user);
		order.updateStatus(OrderStatus.PENDING_PAYMENT);

		orderService().markPaymentApproved(order);

		verify(purchaseEmailSender).sendPurchaseConfirmation(order);
	}

	private OrderService orderService() {
		return new OrderService(
				orderRepository,
				cartItemRepository,
				cartService,
				currentUserService,
				purchaseEmailSender,
				productRealtimePublisher);
	}
}
