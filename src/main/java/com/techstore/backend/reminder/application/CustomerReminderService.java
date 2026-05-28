package com.techstore.backend.reminder.application;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.techstore.backend.config.properties.CustomerReminderProperties;
import com.techstore.backend.favorite.domain.FavoriteProduct;
import com.techstore.backend.favorite.infrastructure.FavoriteProductRepository;
import com.techstore.backend.product.domain.Product;
import com.techstore.backend.product.infrastructure.ProductRepository;
import com.techstore.backend.reminder.domain.ReminderLog;
import com.techstore.backend.reminder.infrastructure.ReminderLogRepository;
import com.techstore.backend.user.domain.AppUser;
import com.techstore.backend.user.domain.Role;
import com.techstore.backend.user.infrastructure.UserRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerReminderService {
	private static final ZoneId DEFAULT_ZONE = ZoneId.of("America/Lima");
	private static final String FAVORITE_TYPE = "FAVORITE_OFFER";
	private static final String RELATED_TYPE = "RELATED_OFFER";

	private final CustomerReminderProperties properties;
	private final UserRepository userRepository;
	private final FavoriteProductRepository favoriteProductRepository;
	private final ProductRepository productRepository;
	private final ReminderLogRepository reminderLogRepository;
	private final CustomerOfferReminderEmailSender emailSender;

	public CustomerReminderService(
			CustomerReminderProperties properties,
			UserRepository userRepository,
			FavoriteProductRepository favoriteProductRepository,
			ProductRepository productRepository,
			ReminderLogRepository reminderLogRepository,
			CustomerOfferReminderEmailSender emailSender) {
		this.properties = properties;
		this.userRepository = userRepository;
		this.favoriteProductRepository = favoriteProductRepository;
		this.productRepository = productRepository;
		this.reminderLogRepository = reminderLogRepository;
		this.emailSender = emailSender;
	}

	@Scheduled(cron = "${app.reminders.customer.cron:0 0 9 * * *}", zone = "America/Lima")
	@Transactional
	public int sendDailyReminders() {
		if (!properties.enabled()) {
			return 0;
		}

		LocalDate today = LocalDate.now(DEFAULT_ZONE);
		int sent = 0;
		for (AppUser user : userRepository.findByEmailVerifiedTrueAndRole(Role.USER)) {
			if (sendReminderFor(user, today)) {
				sent++;
			}
		}
		return sent;
	}

	boolean sendReminderFor(AppUser user, LocalDate today) {
		List<Product> favorites = favoriteProductRepository.findByUserAndProductActiveTrueOrderByIdDesc(user)
				.stream()
				.map(FavoriteProduct::getProduct)
				.toList();
		List<Product> favoriteOffers = favorites.stream()
				.filter(Product::isOnOffer)
				.filter(product -> notLogged(user, product, FAVORITE_TYPE, today))
				.toList();

		Set<Long> favoriteIds = new HashSet<>();
		Set<String> categories = new HashSet<>();
		for (Product product : favorites) {
			favoriteIds.add(product.getId());
			if (product.getCategory() != null && !product.getCategory().isBlank()) {
				categories.add(product.getCategory());
			}
		}

		List<Product> relatedOffers = productRepository.findAll().stream()
				.filter(Product::isActive)
				.filter(Product::isOnOffer)
				.filter(product -> product.getId() != null && !favoriteIds.contains(product.getId()))
				.filter(product -> categories.contains(product.getCategory()))
				.filter(product -> notLogged(user, product, RELATED_TYPE, today))
				.limit(properties.maxRelated())
				.toList();

		if (favoriteOffers.isEmpty() && relatedOffers.isEmpty()) {
			return false;
		}

		boolean sent = emailSender.send(user, favoriteOffers, relatedOffers);
		if (sent) {
			logSent(user, favoriteOffers, FAVORITE_TYPE, today);
			logSent(user, relatedOffers, RELATED_TYPE, today);
		}
		return sent;
	}

	private boolean notLogged(AppUser user, Product product, String type, LocalDate today) {
		return !reminderLogRepository.existsByUserAndProductAndReminderTypeAndReminderDate(user, product, type, today);
	}

	private void logSent(AppUser user, List<Product> products, String type, LocalDate today) {
		for (Product product : products) {
			reminderLogRepository.save(new ReminderLog(user, product, type, today));
		}
	}
}
